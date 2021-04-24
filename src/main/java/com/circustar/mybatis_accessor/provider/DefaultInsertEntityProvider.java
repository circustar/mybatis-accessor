package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.InsertCommand;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.collection.MapOptionUtils;

import java.util.*;

public class DefaultInsertEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultInsertEntityProvider instance = new DefaultInsertEntityProvider();
    public static DefaultInsertEntityProvider getInstance() {
        return instance;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        boolean insertAllEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_INSERT_ALL_CHILDREN, false);
        String[] children;
        if(insertAllEntities) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        }
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);

        String[] topEntities = this.getTopEntities(children, ".");
        boolean hasChildren = false;

        List<Object> updateTargetList = new ArrayList<>();
        for(Object value : values) {
            if(dtoClassInfo.getVersionField() != null) {
                FieldUtils.setFieldValue(value
                        , dtoClassInfo.getVersionField().getWriteMethod()
                        , dtoClassInfo.getVersionDefaultValue());
            }
            Object updateTarget = dtoClassInfoHelper.convertToEntity(value);
            updateTargetList.add(updateTarget);
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , InsertCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singletonList(updateTarget)
                    , false
                    , updateChildrenOnly);
            for(String entityName : topEntities) {
                DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                Object subValue = FieldUtils.getFieldValue(value, dtoField.getReadMethod());
                Collection childList = CollectionUtils.convertToCollection(subValue);
                if(childList.size() == 0) {continue;}
                hasChildren = true;
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, this.getChildren(children
                        , entityName, "."));
                defaultEntityCollectionUpdater.addSubUpdateEntities(this.createUpdateEntities(
                        dtoField.getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, childList, newOptions));
            }
            result.add(defaultEntityCollectionUpdater);
        }

        if(!hasChildren) {
            if(updateChildrenOnly) {
                return Collections.emptyList();
            } else {
                DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                        , InsertCommand.getInstance()
                        , null
                        , dtoClassInfo.getEntityClassInfo()
                        , updateTargetList
                        , false
                        , false);
                return Collections.singletonList(defaultEntityCollectionUpdater);
            }
        }

        return result;
    };

}
