package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.DefaultInsertProviderParam;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.InsertCommand;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.collection.MapOptionUtils;

import java.util.*;

public class DefaultInsertEntityProvider extends AbstractUpdateEntityProvider<DefaultInsertProviderParam> {
    private static DefaultInsertEntityProvider instance = new DefaultInsertEntityProvider();
    public static DefaultInsertEntityProvider getInstance() {
        return instance;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultInsertProviderParam options) {
        return this.createUpdateEntities(relation, dtoClassInfoHelper, dto, options, new HashSet());
    }

    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultInsertProviderParam options, Set updateTargetSet) {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        values.removeAll(updateTargetSet);
        updateTargetSet.addAll(values);
        if(values.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        boolean includeAllChildren = options.isIncludeAllChildren();
        String[] children;
        if(includeAllChildren) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getChildDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = options.getUpdateChildrenNames();
        }
        boolean updateChildrenOnly = options.isUpdateChildrenOnly();

        String[] topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        boolean hasChildren = false;

        List<Object> updateTargetList = new ArrayList<>();
        for(Object value : values) {
            if(value == null) {
                continue;
            }
            if(dtoClassInfo.getVersionField() != null) {
                FieldUtils.setFieldValue(value
                        , dtoClassInfo.getVersionField().getPropertyDescriptor().getWriteMethod()
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
                Object subValue = FieldUtils.getFieldValue(value, dtoField.getPropertyDescriptor().getReadMethod());
                if(subValue == null) {continue;}
                Collection childList = CollectionUtils.convertToCollection(subValue);
                if(childList.isEmpty()) {continue;}
                hasChildren = true;
                DefaultInsertProviderParam subOptions = new DefaultInsertProviderParam(false, includeAllChildren, this.getChildren(children
                        , entityName, DEFAULT_DELIMITER));
                defaultEntityCollectionUpdater.addSubUpdateEntities(this.createUpdateEntities(
                        dtoField.getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, childList, subOptions, updateTargetSet));
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
