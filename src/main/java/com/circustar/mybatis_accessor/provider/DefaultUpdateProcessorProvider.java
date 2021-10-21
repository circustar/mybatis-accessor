package com.circustar.mybatis_accessor.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.*;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.*;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultUpdateProcessorProvider extends AbstractUpdateEntityProvider<DefaultEntityProviderParam> {

    private ISelectService selectService;

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
        String[] children;
        if(options.isIncludeAllChildren()) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = options.getUpdateChildrenNames();
        }

        DefaultDeleteByIdProcessorProvider defaultDeleteByIdProvider = (DefaultDeleteByIdProcessorProvider) PROVIDER_MAP.get(DefaultDeleteByIdProcessorProvider.class);
        DefaultDeleteProcessorProvider defaultDeleteDtoProvider = (DefaultDeleteProcessorProvider) PROVIDER_MAP.get(DefaultDeleteProcessorProvider.class);
        DefaultInsertProcessorProvider insertEntitiesEntityProvider = (DefaultInsertProcessorProvider) PROVIDER_MAP.get(DefaultInsertProcessorProvider.class);

        String[] topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
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
        List<DtoField> dtoFields = Arrays.stream(topEntities).map(x -> dtoClassInfo.getDtoField(x)).collect(Collectors.toList());
        List<DtoField> deleteAndInsertFields = dtoFields.stream().filter(x -> x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        String[] deleteAndInsertFieldNames = deleteAndInsertFields.stream().map(x -> x.getField().getName()).collect(Collectors.toList()).toArray(new String[0]);
        List<DtoField> updateFields = dtoFields.stream().filter(x -> !x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        for(Object updateDto : updateDtoList) {
            Object entity = dtoClassInfoHelper.convertToEntity(updateDto, dtoClassInfo);
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , Collections.singletonList(updateDto)
                    , Collections.singletonList(entity)
                    , this.getUpdateChildrenFirst()
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
                                            , dtoClassInfoHelper, subIds, DefaultEntityProviderParam.IncludeAllEntityProviderParam));
                        }
                    }
                    if(!childList.isEmpty()) {
                        hasChildren = true;
                        DtoClassInfo subDtoClassInfo = subDtoField.getFieldDtoClassInfo();
                        childList.stream().forEach(x -> FieldUtils.setFieldValue(x,subDtoClassInfo.getKeyField().getPropertyDescriptor().getWriteMethod()
                                ,null));
                        String[] subChildren = this.getChildren(children, subDtoField.getField().getName(), DEFAULT_DELIMITER);

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
                String[] subChildren = this.getChildren(children, subDtoField.getField().getName(), DEFAULT_DELIMITER);
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
                    , dtoClassInfoHelper.convertToEntityList(updateDtoList, dtoClassInfo)
                    , this.getUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly()));
        } else {
            result.addAll(updateResult);
        }
        return result;
    }
}
