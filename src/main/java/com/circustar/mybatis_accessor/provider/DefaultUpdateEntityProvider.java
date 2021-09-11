package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.*;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.*;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;

import java.util.*;

public class DefaultUpdateEntityProvider extends AbstractUpdateEntityProvider<DefaultUpdateProviderParam> {
    private static DefaultUpdateEntityProvider instance = new DefaultUpdateEntityProvider();
    public static DefaultUpdateEntityProvider getInstance() {
        return instance;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultUpdateProviderParam options) {
        return this.createUpdateEntities(relation, dtoClassInfoHelper, dto, options, new HashSet());
    }

    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultUpdateProviderParam options
            , Set updateTargetSet) {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        values.removeAll(updateTargetSet);
        updateTargetSet.addAll(values);
        if(values.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        String[] children;
        if(options.isIncludeAllChildren()) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = options.getUpdateChildrenNames();
        }
        boolean physicDelete = dtoClassInfo.isPhysicDelete();

        DefaultDeleteEntityProvider defaultDeleteEntityProvider =  DefaultDeleteEntityProvider.getInstance();
        DefaultInsertEntityProvider insertEntitiesEntityProvider = DefaultInsertEntityProvider.getInstance();
        String keyColumn = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn();

        String[] topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        List deleteObjectList = new ArrayList();
        List insertObjectList = new ArrayList();
        List updateObjectList = new ArrayList();
        for(Object value : values) {
            if(value == null) {continue;}
            Object keyValue = FieldUtils.getFieldValue(value, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
            if(keyValue == null) {
                if(options.isDelegateMode()) {
                    insertObjectList.add(value);
                } else {
                    throw new RuntimeException("cannot update without id");
                }
            } else {
                boolean isDeleted = dtoClassInfo.isObjectDeleted(value);
                if(isDeleted) {
                    deleteObjectList.add(keyValue);
                } else {
                    updateObjectList.add(value);
                }
            }
        }
        if(!deleteObjectList.isEmpty()) {
            DefaultDeleteProviderParam defaultDeleteProviderParam = new DefaultDeleteProviderParam(options);
            result.addAll(defaultDeleteEntityProvider.createUpdateEntities(relation
                    , dtoClassInfoHelper, deleteObjectList, defaultDeleteProviderParam));
        }
        if(!insertObjectList.isEmpty()) {
            DefaultInsertProviderParam defaultInsertProviderParam = new DefaultInsertProviderParam(options);
            result.addAll(insertEntitiesEntityProvider.createUpdateEntities(relation
                    , dtoClassInfoHelper, deleteObjectList, defaultInsertProviderParam));
        }

        boolean hasChildren = false;
        for(Object value : updateObjectList) {
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singletonList(value)
                    , false
                    , options.isUpdateChildrenOnly());

            for(String entityName : topEntities) {
                DtoField subDtoField = dtoClassInfo.getDtoField(entityName);
                Object topChild = FieldUtils.getFieldValue(value, subDtoField.getPropertyDescriptor().getReadMethod());
                if(topChild == null) {continue;}
                Collection childList = CollectionUtils.convertToCollection(topChild);
                if(childList.isEmpty()) {continue;}
                hasChildren = true;
                String[] subChildren = this.getChildren(children, entityName, DEFAULT_DELIMITER);
                DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(subDtoField.getEntityDtoServiceRelation().getDtoClass());
                DefaultUpdateProviderParam subOptions = new DefaultUpdateProviderParam(false
                        , options.isIncludeAllChildren(), subChildren);
                defaultEntityCollectionUpdater.addSubUpdateEntities(
                        this.createUpdateEntities(subDtoClassInfo.getEntityDtoServiceRelation()
                                , dtoClassInfoHelper, childList, subOptions, updateTargetSet));
            }
            result.add(defaultEntityCollectionUpdater);
        }
        if(!hasChildren) {
            return Collections.singletonList(new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , updateObjectList
                    , false
                    , options.isUpdateChildrenOnly()));
        }
        return result;
    }
}
