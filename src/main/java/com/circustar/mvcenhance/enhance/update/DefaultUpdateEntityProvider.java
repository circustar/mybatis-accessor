package com.circustar.mvcenhance.enhance.update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mvcenhance.enhance.field.DtoClassInfo;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.field.DtoField;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.update.command.*;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;

import java.util.*;

public class DefaultUpdateEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultUpdateEntityProvider instance = new DefaultUpdateEntityProvider();
    public static DefaultUpdateEntityProvider getInstance() {
        return instance;
    }
    @Override
    public Collection<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation,
                                                         DtoClassInfoHelper dtoClassInfoHelper,
                                                         Object entity, Map options) throws Exception {
        String[] subEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean deleteBeforeUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_BEFORE_UPDATE, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        boolean batchUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_BATCH_FLAG, false);

        DefaultInsertEntitiesEntityProvider inertEntitiesEntityProvider = DefaultInsertEntitiesEntityProvider.getInstance();

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());
        Collection values;
        if(Collection.class.isAssignableFrom(entity.getClass())) {
            values = (Collection) entity;
        } else {
            values = Collections.singleton(entity);
        }

        List<UpdateEntity> updateEntityCollection = new ArrayList<>();
        String keyColumn = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn();
        String keyProperty = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyProperty();

        for(Object object : values) {
            UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                    , batchUpdate? UpdateByIdBatchCommand.getInstance() : UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(object)
                    , false
                    , updateChildrenOnly);
            Object keyValue = FieldUtils.getValueByName(object, keyProperty);

            for (String subFieldName : subEntities) {
                Object subEntity = FieldUtils.getValueByName(object, subFieldName);
                DtoField subDtoField = dtoClassInfo.getDtoField(subFieldName);
                if(deleteBeforeUpdate) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(keyColumn, keyValue);
                    updateEntity.addSubUpdateEntity(new UpdateEntity(applicationContext.getBean(subDtoField.getEntityDtoServiceRelation().getService())
                            , DeleteWrapperCommand.getInstance()
                            , physicDelete
                            , subDtoField.getDtoClassInfo().getEntityClassInfo()
                            , Collections.singleton(qw)));
                }
                if (subEntity == null) {
                    continue;
                }

                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, true);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, this.getSubEntities(subEntities
                        , subFieldName, "."));
                if(keyValue != null) {
                    updateEntity.addSubUpdateEntities(this.createUpdateEntities(
                            subDtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper
                            , subEntity
                            , newOptions
                    ));
                } else {
                    updateEntity.addSubUpdateEntities(inertEntitiesEntityProvider.createUpdateEntities(subDtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper
                            , subEntity
                            , newOptions
                    ));
                };
            }
            updateEntityCollection.add(updateEntity);
        }
        return updateEntityCollection;
    }
}
