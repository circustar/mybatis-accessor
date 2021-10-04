package com.circustar.mybatis_accessor.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
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
import java.util.stream.Collectors;

public class DefaultUpdateEntityProvider extends AbstractUpdateEntityProvider<DefaultUpdateProviderParam> {
    private static DefaultUpdateEntityProvider instance = new DefaultUpdateEntityProvider();
    public static DefaultUpdateEntityProvider getInstance() {
        return instance;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultUpdateProviderParam options) {
        return this.createUpdateProcessors(relation, dtoClassInfoHelper, dto, options);
    }

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultUpdateProviderParam options) {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        String[] children;
        if(options.isIncludeAllChildren()) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = options.getUpdateChildrenNames();
        }

        DefaultDeleteEntityProvider defaultDeleteEntityProvider =  DefaultDeleteEntityProvider.getInstance();
        DefaultInsertEntityProvider insertEntitiesEntityProvider = DefaultInsertEntityProvider.getInstance();

        String[] topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        List deleteObjectList = new ArrayList();
        List insertObjectList = new ArrayList();
        List updateObjectList = new ArrayList();
        for(Object value : values) {
            if(value == null) {continue;}
            Object keyValue = FieldUtils.getFieldValue(value, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
            if(keyValue == null) {
                insertObjectList.add(value);
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
            result.addAll(defaultDeleteEntityProvider.createUpdateEntities(relation
                    , dtoClassInfoHelper, deleteObjectList, options));
        }
        if(!insertObjectList.isEmpty()) {
            result.addAll(insertEntitiesEntityProvider.createUpdateEntities(relation
                    , dtoClassInfoHelper, insertObjectList, options));
        }

        boolean hasChildren = false;
        List<IEntityUpdateProcessor> updateResult = new ArrayList<>();
        for(Object value : updateObjectList) {
            Object entity = dtoClassInfoHelper.convertToEntity(value);
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , Collections.singletonList(entity)
                    , this.getUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly());

            Object keyValue = FieldUtils.getFieldValue(value, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
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
                if(subDtoField.isDeleteAndInsertNewOnUpdate()) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(relation.getTableInfo().getKeyColumn(), keyValue);
                    IService subEntityService = subDtoClassInfo.getEntityDtoServiceRelation().getServiceBean(applicationContext);
                    List deletedSubEntities = subEntityService.list(qw);
                    List subIds = (List) deletedSubEntities.stream().map(x -> FieldUtils.getFieldValue(x
                            , subDtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod()))
                            .collect(Collectors.toList());
                    defaultEntityCollectionUpdater.addSubUpdateEntities(defaultDeleteEntityProvider.createUpdateEntities(subDtoClassInfo.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper, subIds, DefaultEntityProviderParam.IncludeAllEntityProviderParam));
                    defaultEntityCollectionUpdater.addSubUpdateEntities(insertEntitiesEntityProvider.createUpdateEntities(subDtoClassInfo.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper, childList, subOptions));
                } else {
                    defaultEntityCollectionUpdater.addSubUpdateEntities(
                            this.createUpdateProcessors(subDtoClassInfo.getEntityDtoServiceRelation()
                                    , dtoClassInfoHelper, childList, subOptions));
                }
            }
            updateResult.add(defaultEntityCollectionUpdater);
        }
        if(!hasChildren) {
            result.add(new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , dtoClassInfoHelper.convertToEntityList(updateObjectList)
                    , this.getUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly()));
        } else {
            result.addAll(updateResult);
        }
        return result;
    }
}
