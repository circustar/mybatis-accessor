package com.circustar.mybatis_accessor.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.utils.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.*;
import com.circustar.mybatis_accessor.utils.CollectionUtils;
import com.circustar.mybatis_accessor.utils.FieldUtils;
import com.circustar.mybatis_accessor.utils.MapOptionUtils;

import java.util.*;

public class DefaultUpdateEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultUpdateEntityProvider instance = new DefaultUpdateEntityProvider();
    public static DefaultUpdateEntityProvider getInstance() {
        return instance;
    }
    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());

        String[] childNameList = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean deleteBeforeUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DefaultDeleteEntityProvider defaultDeleteTreeProvider =  DefaultDeleteEntityProvider.getInstance();
        DefaultInsertEntityProvider inertEntitiesEntityProvider = DefaultInsertEntityProvider.getInstance();
        List<IEntityUpdateProcessor> defaultEntityCollectionUpdaterCollection = new ArrayList<>();
        String keyColumn = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn();

        String[] topEntities = this.getTopEntities(childNameList, ".");

        List<Object> updateTargetList = new ArrayList<>();
        for(Object value : values) {
            Object updateTarget = dtoClassInfoHelper.convertToEntity(value);
            updateTargetList.add(updateTarget);

            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singletonList(updateTarget)
                    , false
                    , updateChildrenOnly);
            Object keyValue = FieldUtils.getValue(value, dtoClassInfo.getKeyField().getEntityFieldInfo().getField());

            for(String entityName : topEntities) {
                Object topChild = FieldUtils.getValueByName(value, entityName);
                DtoField subDtoField = dtoClassInfo.getDtoField(entityName);
                if(deleteBeforeUpdate) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(keyColumn, keyValue);
                    defaultEntityCollectionUpdater.addSubUpdateEntity(new DefaultEntityCollectionUpdateProcessor(subDtoField.getEntityDtoServiceRelation().getServiceBean(applicationContext)
                            , DeleteWrapperCommand.getInstance()
                            , physicDelete
                            , null
                            , Collections.singletonList(qw)
                            , true
                            , false));
                }
                Collection childList = CollectionUtils.convertToCollection(topChild);
                if(childList.size() == 0) {continue;}
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, this.getChildren(childNameList
                        , entityName, "."));
                DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(subDtoField.getEntityDtoServiceRelation().getDtoClass());
                for(Object child : childList) {
                    Object subEntityKeyValue = FieldUtils.getValue(child, subDtoClassInfo.getKeyField().getEntityFieldInfo().getField());
                    if(subEntityKeyValue != null) {
                        Object deleteFlagValue = null;
                        if(subDtoClassInfo.getDeleteFlagField() != null) {
                            deleteFlagValue = FieldUtils.getValue(child, subDtoClassInfo.getDeleteFlagField().getEntityFieldInfo().getField());
                        }
                        if(deleteFlagValue != null) {
                            defaultEntityCollectionUpdater.addSubUpdateEntities(defaultDeleteTreeProvider.createUpdateEntities(
                                    subDtoField.getEntityDtoServiceRelation(), dtoClassInfoHelper
                                    , subEntityKeyValue, newOptions
                            ));
                        } else {
                            defaultEntityCollectionUpdater.addSubUpdateEntities(this.createUpdateEntities(
                                    subDtoField.getEntityDtoServiceRelation(), dtoClassInfoHelper
                                    , child, newOptions
                            ));
                        }
                    } else {
                        defaultEntityCollectionUpdater.addSubUpdateEntities(inertEntitiesEntityProvider.createUpdateEntities(subDtoField.getEntityDtoServiceRelation()
                                , dtoClassInfoHelper, child, newOptions));
                    }
                }
            }
            defaultEntityCollectionUpdaterCollection.add(defaultEntityCollectionUpdater);
        }
        return defaultEntityCollectionUpdaterCollection;
    }
}
