package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.*;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.update_processor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.*;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.update_processor.UpdateDtoUpdateProcessor;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultUpdateProcessorProvider extends AbstractUpdateEntityProvider<DefaultEntityProviderParam> {

    public DefaultUpdateProcessorProvider(ApplicationContext applicationContext, ISelectService selectService) {
        super(applicationContext);
        this.selectService = selectService;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultEntityProviderParam options) {
        return this.createUpdateProcessors(relation, dtoClassInfoHelper, dto, options);
    }

    private List addUpdateProcessor(List<IEntityUpdateProcessor> processorList, DtoClassInfo dtoClassInfo
            , List dtoList, DefaultEntityProviderParam options) {
        return dtoList;
    }

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultEntityProviderParam options) {
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation);
//        final Method keyReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        List dtoList = CollectionUtils.convertToList(dto);
        if(dtoList.isEmpty()) {return result;}

        List<String> children = null;
        if(!options.isIncludeAllChildren()) {
            children = options.getUpdateChildrenNames();
        }

        List updateDtoList = this.addUpdateProcessor(result, dtoClassInfo, dtoList, options);
        if(updateDtoList.isEmpty()) {
            return result;
        }

        boolean hasChildren = false;
        List<IEntityUpdateProcessor> updateResult = new ArrayList<>();
        List<String> topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        List<DtoField> dtoFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, options.isIncludeAllChildren(), true, topEntities);
        List<DtoField> deleteAndInsertFields = dtoFields.stream().filter(x -> x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        List<DtoField> updateFields = dtoFields.stream().filter(x -> !x.isDeleteAndInsertNewOnUpdate()).collect(Collectors.toList());
        for(Object updateDto : updateDtoList) {
            UpdateDtoUpdateProcessor defaultEntityCollectionUpdater = new UpdateDtoUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , Collections.singletonList(updateDto)
                    , this.isUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly());

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
            result.add(new UpdateDtoUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , updateDtoList
                    , this.isUpdateChildrenFirst()
                    , options.isUpdateChildrenOnly()));
        } else {
            result.addAll(updateResult);
        }
        return result;
    }
}
