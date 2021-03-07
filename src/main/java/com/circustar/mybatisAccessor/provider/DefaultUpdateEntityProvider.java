package com.circustar.mybatisAccessor.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatisAccessor.classInfo.DtoClassInfo;
import com.circustar.mybatisAccessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatisAccessor.classInfo.DtoField;
import com.circustar.mybatisAccessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatisAccessor.utils.MvcEnhanceConstants;
import com.circustar.mybatisAccessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatisAccessor.provider.command.*;
import com.circustar.mybatisAccessor.utils.CollectionUtils;
import com.circustar.mybatisAccessor.utils.FieldUtils;
import com.circustar.mybatisAccessor.utils.MapOptionUtils;

import java.util.*;

public class DefaultUpdateEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultUpdateEntityProvider instance = new DefaultUpdateEntityProvider();
    public static DefaultUpdateEntityProvider getInstance() {
        return instance;
    }
    @Override
    public List<DefaultEntityCollectionUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception {
        List<DefaultEntityCollectionUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());

        String[] childNameList = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean deleteBeforeUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DefaultDeleteEntityProvider defaultDeleteTreeProvider =  DefaultDeleteEntityProvider.getInstance();
        DefaultInsertEntityProvider inertEntitiesEntityProvider = DefaultInsertEntityProvider.getInstance();
        List<DefaultEntityCollectionUpdateProcessor> defaultEntityCollectionUpdaterCollection = new ArrayList<>();
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
                    , Collections.singleton(updateTarget)
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
                            , Collections.singleton(qw)
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
