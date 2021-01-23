package com.circustar.mvcenhance.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.command.*;
import com.circustar.mvcenhance.utils.CommonCollectionUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.MapOptionUtils;

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
        List<UpdateEntity> result = Collections.emptyList();
        Collection values = CommonCollectionUtils.convertToCollection(entity);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());

        String[] subEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean deleteBeforeUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_BEFORE_UPDATE, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DefaultInsertEntitiesProvider inertEntitiesEntityProvider = DefaultInsertEntitiesProvider.getInstance();
        List<UpdateEntity> updateEntityCollection = new ArrayList<>();
        String keyColumn = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn();
        String keyProperty = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyProperty();

        String[] topEntities = this.getTopEntities(subEntities, ".");
        boolean containSubEntities = false;

        for(Object object : values) {
            UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(object)
                    , false
                    , updateChildrenOnly);
            Object keyValue = FieldUtils.getValueByName(object, keyProperty);

            for(String entityName : topEntities) {
                Object subEntity = FieldUtils.getValueByName(object, entityName);
                DtoField subDtoField = dtoClassInfo.getDtoField(entityName);
                if(deleteBeforeUpdate) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(keyColumn, keyValue);
                    updateEntity.addSubUpdateEntity(new UpdateEntity(applicationContext.getBean(subDtoField.getEntityDtoServiceRelation().getService())
                            , DeleteWrapperCommand.getInstance()
                            , physicDelete
                            , subDtoField.getDtoClassInfo().getEntityClassInfo()
                            , Collections.singleton(qw)
                            , true
                            , false));
                    containSubEntities = true;
                }
                Collection subEntityList = CommonCollectionUtils.convertToCollection(subEntity);
                if(subEntityList.size() == 0) {continue;}
                containSubEntities = true;
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, this.getSubEntities(subEntities
                        , entityName, "."));
                if(keyValue != null) {
                    updateEntity.addSubUpdateEntities(this.createUpdateEntities(
                            subDtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper
                            , subEntityList
                            , newOptions
                    ));
                } else {
                    updateEntity.addSubUpdateEntities(inertEntitiesEntityProvider.createUpdateEntities(subDtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper
                            , subEntityList
                            , newOptions
                    ));
                };
            }
            updateEntityCollection.add(updateEntity);
        }
        if(!containSubEntities) {
            if (updateChildrenOnly) {
                return Collections.emptyList();
            } else {
                UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                        , UpdateByIdBatchCommand.getInstance()
                        , null
                        , dtoClassInfo.getEntityClassInfo()
                        , values
                        , false
                        , false);
                return Collections.singletonList(updateEntity);
            }
        }
        return updateEntityCollection;
    }
}
