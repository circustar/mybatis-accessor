package com.circustar.mybatis_accessor.provider;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.DeleteEntityCommand;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.IEntityProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultDeleteProcessorProvider extends AbstractUpdateEntityProvider<IEntityProviderParam> {

    public DefaultDeleteProcessorProvider(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected boolean getUpdateChildrenFirst() {
        return true;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, IEntityProviderParam options) {
        return this.createUpdateProcessors(relation, dtoClassInfoHelper, ids, options);
    }

    protected Object convertToUpdateTarget(DtoClassInfoHelper dtoClassInfoHelper, DtoClassInfo dtoClassInfo, Object obj) {
        return dtoClassInfoHelper.convertToEntity(obj);
    }

    protected List convertToSubUpdateList(DtoClassInfoHelper dtoClassInfoHelper, DtoClassInfo dtoClassInfo, List obj) {
        return obj;
    }

    protected Object getUpdateId(Object obj, DtoField keyField) {
        return FieldUtils.getFieldValue(obj, keyField.getPropertyDescriptor().getReadMethod());
    }

    protected IUpdateCommand getUpdateCommand() {
        return DeleteEntityCommand.getInstance();
    }

    protected Object createUpdateProcessorParam(DtoClassInfo dtoClassInfo, IEntityProviderParam options) {
        Map<String, Object> map = new HashMap<>();
        map.put(DeleteEntityCommand.PHYSIC_DELETE, dtoClassInfo.isPhysicDelete());
        map.put(DeleteEntityCommand.KEY_FIELD_READ_METHOD, dtoClassInfo.getEntityClassInfo()
                .getKeyField().getPropertyDescriptor().getReadMethod());
        return map;
    }

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dtoParam, IEntityProviderParam options)
    {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        List dtoList = CollectionUtils.convertToList(dtoParam);
        if(dtoList.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        String[] children;
        if(options.isIncludeAllChildren()) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getChildDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = options.getUpdateChildrenNames();
        }

        ISelectService selectService = this.getSelectService();

        String[] topEntities = this.getTopEntities(dtoClassInfo, children, DEFAULT_DELIMITER);
        DefaultEntityCollectionUpdateProcessor updateProcessor;
        List updateEntityWithNoSubList = new ArrayList();
        List updateDtoWithNoSubList = new ArrayList();
        Object updateProcessorParam = createUpdateProcessorParam(dtoClassInfo, options);

        if(topEntities == null || topEntities.length == 0) {
            updateDtoWithNoSubList = dtoList;
            updateEntityWithNoSubList = (List) dtoList.stream()
                    .map(x -> this.convertToUpdateTarget(dtoClassInfoHelper, dtoClassInfo, x))
                    .collect(Collectors.toList());
        } else {
            for (Object dto : dtoList) {
                Serializable id = (Serializable) getUpdateId(dto, dtoClassInfo.getKeyField());
                if(id == null) {
                    throw new RuntimeException(relation.getEntityClass().getSimpleName()
                            + " delete exception : id could not be null : " + id);
                }
                List<IEntityUpdateProcessor> subUpdateEntities = new ArrayList<>();
                Object object = selectService.getDtoById(relation, id,false , topEntities);
                if(object == null) {
                    throw new RuntimeException(relation.getEntityClass().getSimpleName()
                            + " delete exception : key not found : " + id);
                }
                for (String entityName : topEntities) {
                    DtoField subDtoField = dtoClassInfo.getDtoField(entityName);
                    Object subDto = FieldUtils.getFieldValue(object, subDtoField.getPropertyDescriptor().getReadMethod());
                    if (subDto == null) { continue; }
                    DtoClassInfo subDtoClassInfo = subDtoField.getFieldDtoClassInfo();

                    Collection childList = this.convertToSubUpdateList(dtoClassInfoHelper, subDtoClassInfo, CollectionUtils.convertToList(subDto));
                    if(childList.isEmpty()) {continue;}
                    IEntityProviderParam subOptions = new DefaultEntityProviderParam(false
                            , options.isIncludeAllChildren(), this.getChildren(children
                            , entityName, DEFAULT_DELIMITER));
                    subUpdateEntities.addAll(this.createUpdateEntities(
                            subDtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper, childList, subOptions
                    ));
                }
                if(subUpdateEntities.isEmpty()) {
                    updateDtoWithNoSubList.add(dto);
                    updateEntityWithNoSubList.add(this.convertToUpdateTarget(dtoClassInfoHelper, dtoClassInfo, dto));
                    continue;
                }

                if(options.isUpdateChildrenOnly()) {
                    result.addAll(subUpdateEntities);
                } else {
                    updateProcessor = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                            , getUpdateCommand()
                            , updateProcessorParam
                            , dtoClassInfo
                            , Collections.singletonList(dto)
                            , Collections.singletonList(this.convertToUpdateTarget(dtoClassInfoHelper, dtoClassInfo, dto))
                            , this.getUpdateChildrenFirst()
                            , false);
                    updateProcessor.addSubUpdateEntities(subUpdateEntities);
                    result.add(updateProcessor);
                }
            }
        }
        if(!options.isUpdateChildrenOnly() && !updateEntityWithNoSubList.isEmpty()) {
            updateProcessor = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , getUpdateCommand()
                    , updateProcessorParam
                    , dtoClassInfo
                    , updateDtoWithNoSubList
                    , updateEntityWithNoSubList
                    , this.getUpdateChildrenFirst()
                    , false);
            result.add(updateProcessor);
        }

        return result;
    }
}
