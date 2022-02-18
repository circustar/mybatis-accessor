package com.circustar.mybatis_accessor.provider;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.provider.command.DeleteByIdBatchCommand;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.IEntityProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.update_processor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.update_processor.IEntityUpdateProcessor;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultDeleteProcessorProvider extends AbstractUpdateEntityProvider<IEntityProviderParam> {

    public DefaultDeleteProcessorProvider(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, IEntityProviderParam options) {
        return this.createUpdateProcessors(relation, dtoClassInfoHelper, ids, options);
    }

    protected Object convertToUpdateTarget(DtoClassInfo dtoClassInfo, Object obj) {
        Method readMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        return FieldUtils.getFieldValue(obj, readMethod);
    }

    protected List convertToSubUpdateList(DtoClassInfoHelper dtoClassInfoHelper, DtoClassInfo dtoClassInfo, List obj) {
        return obj;
    }

    protected Object getUpdateId(Object obj, DtoField keyField) {
        return FieldUtils.getFieldValue(obj, keyField.getPropertyDescriptor().getReadMethod());
    }

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dtoParam, IEntityProviderParam options)
    {
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation);
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        List dtoList = (List) CollectionUtils.convertToList(dtoParam).stream().sorted((x, y) -> {
            Comparable k1 = (Comparable) getUpdateId(x, dtoClassInfo.getKeyField());
            Comparable k2 = (Comparable) getUpdateId(y, dtoClassInfo.getKeyField());
            return k1.compareTo(k2);
        }).collect(Collectors.toList());
        if(dtoList.isEmpty()) {return result;}

        List<String> children;
        if(options.isIncludeAllChildren()) {
            children = dtoClassInfo.getUpdateCascadeDtoFieldList().stream().map(x -> x.getField().getName()).collect(Collectors.toList());
        } else {
            children = options.getUpdateChildrenNames();
        }

        ISelectService selectService = this.getSelectService();

        List<String> topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        DefaultEntityCollectionUpdateProcessor updateProcessor;
        List updateEntityWithNoSubList = new ArrayList();

        if(org.springframework.util.CollectionUtils.isEmpty(topEntities)) {
            updateEntityWithNoSubList = (List) dtoList.stream()
                    .map(x -> this.convertToUpdateTarget(dtoClassInfo, x))
                    .collect(Collectors.toList());
        } else {
            List<DtoField> dtoFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, options.isIncludeAllChildren(), true, topEntities);
            List<IEntityUpdateProcessor> subUpdateEntities = new ArrayList<>();
            for (Object dto : dtoList) {
                Serializable id = (Serializable) getUpdateId(dto, dtoClassInfo.getKeyField());
                Object object = selectService.getDtoById(relation, id,false , topEntities);
                subUpdateEntities.clear();
                for (DtoField subDtoField : dtoFields) {
                    Object subDto = FieldUtils.getFieldValue(object, subDtoField.getPropertyDescriptor().getReadMethod());
                    if (subDto == null) { continue; }
                    DtoClassInfo subDtoClassInfo = subDtoField.getFieldDtoClassInfo();

                    Collection childList = this.convertToSubUpdateList(dtoClassInfoHelper, subDtoClassInfo, CollectionUtils.convertToList(subDto));
                    if(childList.isEmpty()) {continue;}
                    IEntityProviderParam subOptions = new DefaultEntityProviderParam(false
                            , options.isIncludeAllChildren(), this.getChildren(children
                            , subDtoField.getField().getName(), DEFAULT_DELIMITER));
                    subUpdateEntities.addAll(this.createUpdateEntities(
                            subDtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper, childList, subOptions
                    ));
                }
                if(subUpdateEntities.isEmpty()) {
                    updateEntityWithNoSubList.add(this.convertToUpdateTarget(dtoClassInfo, dto));
                    continue;
                }

                if(options.isUpdateChildrenOnly()) {
                    result.addAll(subUpdateEntities);
                } else {
                    List<Object> updateList = Collections.singletonList(this.convertToUpdateTarget(dtoClassInfo, dto));
                    updateProcessor = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                            , DeleteByIdBatchCommand.getInstance()
                            , null
                            , dtoClassInfo
                            , updateList
                            , false
                            , this.isUpdateChildrenFirst()
                            , false);
                    updateProcessor.addSubUpdateEntities(subUpdateEntities);
                    result.add(updateProcessor);
                }
            }
        }
        if(!options.isUpdateChildrenOnly() && !updateEntityWithNoSubList.isEmpty()) {
            updateProcessor = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , DeleteByIdBatchCommand.getInstance()
                    , null
                    , dtoClassInfo
                    , updateEntityWithNoSubList
                    , false
                    , this.isUpdateChildrenFirst()
                    , false);
            result.add(updateProcessor);
        }

        return result;
    }
}
