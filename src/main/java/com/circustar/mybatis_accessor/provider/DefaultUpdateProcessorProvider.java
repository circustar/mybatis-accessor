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

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultEntityProviderParam options) {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection dtoList = CollectionUtils.convertToList(dto);
        if(dtoList.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation);
        List<String> children = null;
        if(!options.isIncludeAllChildren()) {
            children = options.getUpdateChildrenNames();
        }

        DefaultDeleteByIdProcessorProvider defaultDeleteByIdProvider = (DefaultDeleteByIdProcessorProvider) getProviderMap().get(DefaultDeleteByIdProcessorProvider.class);
        DefaultDeleteProcessorProvider defaultDeleteDtoProvider = (DefaultDeleteProcessorProvider) getProviderMap().get(DefaultDeleteProcessorProvider.class);
        DefaultInsertProcessorProvider insertEntitiesEntityProvider = (DefaultInsertProcessorProvider) getProviderMap().get(DefaultInsertProcessorProvider.class);

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
            result.addAll(defaultDeleteDtoProvider.createUpdateEntities(relation
                    , dtoClassInfoHelper, deleteDtoList, options));
        }
        if(!insertDtoList.isEmpty()) {
            result.addAll(insertEntitiesEntityProvider.createUpdateEntities(relation
                    , dtoClassInfoHelper, insertDtoList, options));
        }

        boolean hasChildren = false;
        List<IEntityUpdateProcessor> updateResult = new ArrayList<>();
        List<String> topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        List<DtoField> dtoFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, options.isIncludeAllChildren(), true, topEntities);
        List<DtoField> deleteAndInsertFields = dtoFields.stream().filter(x -> x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        List<String> deleteAndInsertFieldNames = deleteAndInsertFields.stream().map(x -> x.getField().getName()).collect(Collectors.toList());
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

            Serializable keyValue = (Serializable) FieldUtils.getFieldValue(updateDto, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
            if(!deleteAndInsertFields.isEmpty()) {
                Object oldDto = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), keyValue, false, deleteAndInsertFieldNames);
                for(DtoField subDtoField : deleteAndInsertFields) {
                    Collection childList = CollectionUtils.convertToList(FieldUtils.getFieldValue(updateDto, subDtoField.getPropertyDescriptor().getReadMethod()));
                    if(subDtoField.isDeleteEvenIfEmpty() || !childList.isEmpty()) {
                        List deletedSubEntities = CollectionUtils.convertToList(FieldUtils.getFieldValue(oldDto, subDtoField.getPropertyDescriptor().getReadMethod()));
                        if(!deletedSubEntities.isEmpty()) {
                            hasChildren = true;
                            List subIds = (List) deletedSubEntities.stream().map(x -> FieldUtils.getFieldValue(x
                                    , subDtoField.getFieldDtoClassInfo().getKeyField().getPropertyDescriptor().getReadMethod()))
                                    .collect(Collectors.toList());
                            defaultEntityCollectionUpdater.addSubUpdateEntities(defaultDeleteByIdProvider
                                    .createUpdateEntities(subDtoField.getFieldDtoClassInfo().getEntityDtoServiceRelation()
                                            , dtoClassInfoHelper, subIds, DefaultEntityProviderParam.INCLUDE_ALL_ENTITY_PROVIDER_PARAM));
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
                                , dtoClassInfoHelper, childList, subOptions));
                    }
                }
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
