package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.*;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.update_processor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.update_processor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.*;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultUpdateProcessorProvider extends AbstractUpdateEntityProvider<DefaultEntityProviderParam> {

    private final ISelectService selectService;

    public DefaultUpdateProcessorProvider(ApplicationContext applicationContext, ISelectService selectService) {
        super(applicationContext);
        this.selectService = selectService;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultEntityProviderParam options) {
        return this.createUpdateProcessors(relation, dtoClassInfoHelper, dto, options);
    }

    private List addInsertOrUpdateProcessor(List<IEntityUpdateProcessor> processorList, DtoClassInfo dtoClassInfo
            , Collection dtoList, DefaultEntityProviderParam options
            , DefaultInsertProcessorProvider insertEntitiesEntityProvider
            , DefaultDeleteProcessorProvider defaultDeleteDtoProvider) {
        List deleteDtoList = new ArrayList();
        List insertDtoList = new ArrayList();
        List updateDtoList = new ArrayList();
        for(Object value : dtoList) {
            if(value == null) {continue;}
            Object keyValue = FieldUtils.getFieldValue(value, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
            if(keyValue == null) {
                insertDtoList.add(value);
            } else {
                if (dtoClassInfo.isObjectDeleted(value)) {
                    deleteDtoList.add(value);
                } else {
                    updateDtoList.add(value);
                }
            }
        }
        if(!deleteDtoList.isEmpty()) {
            processorList.addAll(defaultDeleteDtoProvider.createUpdateEntities(dtoClassInfo.getEntityDtoServiceRelation()
                    , dtoClassInfo.getDtoClassInfoHelper(), deleteDtoList, options));
        }
        if(!insertDtoList.isEmpty()) {
            processorList.addAll(insertEntitiesEntityProvider.createUpdateEntities(dtoClassInfo.getEntityDtoServiceRelation()
                    , dtoClassInfo.getDtoClassInfoHelper(), insertDtoList, options));
        }
        return updateDtoList;
    }

    private boolean addDeleteAndInsertProcessor(DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater
            , DtoClassInfo dtoClassInfo, List<DtoField> deleteAndInsertFields, List<String> children, Object updateDto
            , DefaultEntityProviderParam options, DefaultInsertProcessorProvider insertEntitiesEntityProvider
            , DefaultDeleteProcessorProvider defaultDeleteDtoProvider) {
        if (deleteAndInsertFields.isEmpty()) {
            return false;
        }
        List<String> deleteAndInsertFieldNames = deleteAndInsertFields.stream().map(x -> x.getField().getName()).collect(Collectors.toList());
        Serializable keyValue = (Serializable) FieldUtils.getFieldValue(updateDto, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
        boolean hasChildren = false;
        Object oldDto = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), keyValue, false, deleteAndInsertFieldNames);
        for(DtoField subDtoField : deleteAndInsertFields) {
            Collection childList = CollectionUtils.convertToList(FieldUtils.getFieldValue(updateDto, subDtoField.getPropertyDescriptor().getReadMethod()));
            if(subDtoField.isDeleteEvenIfEmpty() || !childList.isEmpty()) {
                List deletedSubEntities = CollectionUtils.convertToList(FieldUtils.getFieldValue(oldDto, subDtoField.getPropertyDescriptor().getReadMethod()));
                if(!deletedSubEntities.isEmpty()) {
                    hasChildren = true;
                    defaultEntityCollectionUpdater.addSubUpdateEntities(defaultDeleteDtoProvider
                            .createUpdateEntities(subDtoField.getFieldDtoClassInfo().getEntityDtoServiceRelation()
                                    , dtoClassInfo.getDtoClassInfoHelper(), deletedSubEntities, DefaultEntityProviderParam.INCLUDE_ALL_ENTITY_PROVIDER_PARAM));
                }
            }
            if(!childList.isEmpty()) {
                hasChildren = true;
                DtoClassInfo subDtoClassInfo = subDtoField.getFieldDtoClassInfo();
                childList.stream().forEach(x -> FieldUtils.setFieldValue(x,subDtoClassInfo.getKeyField().getPropertyDescriptor().getWriteMethod()
                        ,null));
                List<String> subChildren = this.getChildren(children, subDtoField.getField().getName(), DEFAULT_DELIMITER);

                DefaultEntityProviderParam subOptions = new DefaultEntityProviderParam(false
                        , options.isIncludeAllChildren(), subChildren);
                defaultEntityCollectionUpdater.addSubUpdateEntities(insertEntitiesEntityProvider.createUpdateEntities(subDtoClassInfo.getEntityDtoServiceRelation()
                        , dtoClassInfo.getDtoClassInfoHelper(), childList, subOptions));
            }
        }
        return hasChildren;
    }

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultEntityProviderParam options) {
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation);
        final Method keyReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection dtoList = (Collection) CollectionUtils.convertToList(dto).stream().sorted((x, y) -> {
            Comparable k1 = (Comparable) FieldUtils.getFieldValue(x, keyReadMethod);
            Comparable k2 = (Comparable) FieldUtils.getFieldValue(y, keyReadMethod);
            if(k1 == null) {
                return 1;
            } else if (k2 == null) {
                return -1;
            } else {
                return k1.compareTo(k2);
            }
        }).collect(Collectors.toList());
        if(dtoList.isEmpty()) {return result;}

        List<String> children = null;
        if(!options.isIncludeAllChildren()) {
            children = options.getUpdateChildrenNames();
        }

        DefaultDeleteProcessorProvider defaultDeleteDtoProvider = (DefaultDeleteProcessorProvider) getProviderMap().get(DefaultDeleteProcessorProvider.class);
        DefaultInsertProcessorProvider insertEntitiesEntityProvider = (DefaultInsertProcessorProvider) getProviderMap().get(DefaultInsertProcessorProvider.class);

        List updateDtoList = this.addInsertOrUpdateProcessor(result, dtoClassInfo, dtoList, options
                , insertEntitiesEntityProvider, defaultDeleteDtoProvider);

        boolean hasChildren = false;
        List<IEntityUpdateProcessor> updateResult = new ArrayList<>();
        List<String> topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        List<DtoField> dtoFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, options.isIncludeAllChildren(), true, topEntities);
        List<DtoField> deleteAndInsertFields = dtoFields.stream().filter(x -> x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        List<DtoField> updateFields = dtoFields.stream().filter(x -> !x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        for(Object updateDto : updateDtoList) {
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , Collections.singletonList(updateDto)
                    , true
                    , this.isUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly());

            if(addDeleteAndInsertProcessor(defaultEntityCollectionUpdater, dtoClassInfo, deleteAndInsertFields
                    , children, updateDto, options, insertEntitiesEntityProvider, defaultDeleteDtoProvider)) {
                hasChildren = true;
            }
            for(DtoField subDtoField : updateFields) {
                Collection childList = CollectionUtils.convertToList(FieldUtils.getFieldValue(updateDto, subDtoField.getPropertyDescriptor().getReadMethod()));
                if(childList.isEmpty()) {continue;}
                hasChildren = true;
                List<String> subChildren = this.getChildren(children, subDtoField.getField().getName(), DEFAULT_DELIMITER);
                DtoClassInfo subDtoClassInfo = subDtoField.getFieldDtoClassInfo();
                DefaultEntityProviderParam subOptions = new DefaultEntityProviderParam(false
                        , options.isIncludeAllChildren(), subChildren);
                defaultEntityCollectionUpdater.addSubUpdateEntities(
                        this.createUpdateProcessors(subDtoClassInfo.getEntityDtoServiceRelation()
                                , dtoClassInfoHelper, childList, subOptions));
            }
            updateResult.add(defaultEntityCollectionUpdater);
        }
        if(!hasChildren) {
            result.add(new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , updateDtoList
                    , true
                    , this.isUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly()));
        } else {
            result.addAll(updateResult);
        }
        return result;
    }
}
