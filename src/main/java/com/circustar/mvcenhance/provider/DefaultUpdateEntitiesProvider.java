package com.circustar.mvcenhance.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.command.*;
import com.circustar.mvcenhance.utils.CollectionUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.MapOptionUtils;

import java.util.*;

public class DefaultUpdateEntitiesProvider extends AbstractUpdateEntityProvider {
    private static DefaultUpdateEntitiesProvider instance = new DefaultUpdateEntitiesProvider();
    public static DefaultUpdateEntitiesProvider getInstance() {
        return instance;
    }
    @Override
    public Collection<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception {
        List<UpdateEntity> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());

        String[] childNameList = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean deleteBeforeUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DefaultDeleteEntitiesProvider defaultDeleteEntitiesProvider =  DefaultDeleteEntitiesProvider.getInstance();
        DefaultInsertEntitiesProvider inertEntitiesEntityProvider = DefaultInsertEntitiesProvider.getInstance();
        List<UpdateEntity> updateEntityCollection = new ArrayList<>();
        String keyColumn = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn();

        String[] topEntities = this.getTopEntities(childNameList, ".");

        List<Object> updateTargetList = new ArrayList<>();
        for(Object value : values) {
            Object updateTarget = dtoClassInfoHelper.convertToEntity(value);
            updateTargetList.add(updateTarget);

            UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(updateTarget)
                    , false
                    , updateChildrenOnly);
            Object keyValue = FieldUtils.getValue(value, dtoClassInfo.getKeyField().getFieldTypeInfo().getField());

            for(String entityName : topEntities) {
                Object topChild = FieldUtils.getValueByName(value, entityName);
                DtoField subDtoField = dtoClassInfo.getDtoField(entityName);
                if(deleteBeforeUpdate) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(keyColumn, keyValue);
                    updateEntity.addSubUpdateEntity(new UpdateEntity(applicationContext.getBean(subDtoField.getEntityDtoServiceRelation().getService())
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
                DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(subDtoField.getEntityDtoServiceRelation().getDto());
                for(Object child : childList) {
                    Object subEntityKeyValue = FieldUtils.getValue(child, subDtoClassInfo.getKeyField().getFieldTypeInfo().getField());
                    if(subEntityKeyValue != null) {
                        Object deleteFlagValue = null;
                        if(subDtoClassInfo.getDeleteFlagField() != null) {
                            deleteFlagValue = FieldUtils.getValue(child, subDtoClassInfo.getDeleteFlagField().getFieldTypeInfo().getField());
                        }
                        if(deleteFlagValue != null) {
                            updateEntity.addSubUpdateEntities(defaultDeleteEntitiesProvider.createUpdateEntities(
                                    subDtoField.getEntityDtoServiceRelation(), dtoClassInfoHelper
                                    , subEntityKeyValue, newOptions
                            ));
                        } else {
                            updateEntity.addSubUpdateEntities(this.createUpdateEntities(
                                    subDtoField.getEntityDtoServiceRelation(), dtoClassInfoHelper
                                    , child, newOptions
                            ));
                        }
                    } else {
                        updateEntity.addSubUpdateEntities(inertEntitiesEntityProvider.createUpdateEntities(subDtoField.getEntityDtoServiceRelation()
                                , dtoClassInfoHelper, child, newOptions));
                    }
                }
            }
            updateEntityCollection.add(updateEntity);
        }
        return updateEntityCollection;
    }
}
