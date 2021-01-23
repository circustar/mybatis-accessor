package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.command.InsertCommand;
import com.circustar.mvcenhance.utils.CommonCollectionUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.MapOptionUtils;

import java.util.*;

public class DefaultInsertEntitiesProvider extends AbstractUpdateEntityProvider {
    private static DefaultInsertEntitiesProvider instance = new DefaultInsertEntitiesProvider();
    public static DefaultInsertEntitiesProvider getInstance() {
        return instance;
    }

    @Override
    public Collection<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Map options) throws Exception {
        List<UpdateEntity> result = Collections.emptyList();
        Collection values = CommonCollectionUtils.convertToCollection(object);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());
        boolean insertAllEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_INSERT_ALL_SUB_ENTITIES, false);
        String[] subEntities;
        if(insertAllEntities) {
            subEntities = CommonCollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getFieldName()));
        } else {
            subEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, new String[]{});
        }
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);

        String[] topEntities = this.getTopEntities(subEntities, ".");
        boolean containSubEntities = false;

        for(Object value : values) {
            UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                    , InsertCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(value)
                    , false
                    , updateChildrenOnly);
            for(String entityName : topEntities) {
                DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                Object subValue = FieldUtils.getValue(value, dtoField.getFieldTypeInfo().getField());
                Collection subEntityList = CommonCollectionUtils.convertToCollection(subValue);
                if(subEntityList.size() == 0) {continue;}
                containSubEntities = true;
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, this.getSubEntities(subEntities
                        , entityName, "."));
                updateEntity.addSubUpdateEntities(this.createUpdateEntities(dtoField.getDtoClassInfo().getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, subEntityList, newOptions));
            }
            result.add(updateEntity);
        }

        if(!containSubEntities) {
            if(updateChildrenOnly) {
                return Collections.emptyList();
            } else {
                UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                        , InsertCommand.getInstance()
                        , null
                        , dtoClassInfo.getEntityClassInfo()
                        , values
                        , false
                        , false);
                return Collections.singletonList(updateEntity);
            }
        }

        return result;
    };

}
