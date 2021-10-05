package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.DeleteByIdCommand;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.IEntityProviderParam;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.provider.command.DeleteByIdBatchCommand;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;

import java.io.Serializable;
import java.util.*;

public class DefaultDeleteEntityProvider extends AbstractUpdateEntityProvider<IEntityProviderParam> {
    private static DefaultDeleteEntityProvider instance = new DefaultDeleteEntityProvider();
    public static DefaultDeleteEntityProvider getInstance() {
        return instance;
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

    protected List<IEntityUpdateProcessor> createUpdateProcessors(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, IEntityProviderParam options)
    {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToList(ids);;
        if(values.isEmpty()) {return result;}

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
        List noSubEntityList = new ArrayList();
        if(topEntities == null || topEntities.length == 0) {
            noSubEntityList = Arrays.asList(values.toArray());
        } else {
            for (Object id : values) {
                if(id == null) {
                    throw new RuntimeException("delete exception : id could not be null");
                }
                List<IEntityUpdateProcessor> subUpdateEntities = new ArrayList<>();
                Object object = selectService.getDtoById(relation, (Serializable) id
                        ,false , topEntities);
                if(object == null) {
                    continue;
                }
                for (String entityName : topEntities) {
                    DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                    Object entity = FieldUtils.getFieldValue(object, dtoField.getPropertyDescriptor().getReadMethod());
                    if (entity == null) { continue; }
                    DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(dtoField.getActualClass());

                    DtoField subDtoKeyField = subDtoClassInfo.getKeyField();
                    if(subDtoKeyField == null) {continue;}
                    List<Object> subIds = new ArrayList<>();
                    Collection entityList = CollectionUtils.convertToList(entity);
                    if(entityList.isEmpty()) {continue;}
                    for (Object obj : entityList) {
                        Object subId = FieldUtils.getFieldValue(obj, subDtoKeyField.getPropertyDescriptor().getReadMethod());
                        if(subId != null) {subIds.add(subId);}
                    }

                    IEntityProviderParam subOptions = new DefaultEntityProviderParam(false
                            , options.isIncludeAllChildren(), this.getChildren(children
                            , entityName, DEFAULT_DELIMITER));
                    subUpdateEntities.addAll(this.createUpdateEntities(
                            dtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper, subIds, subOptions
                    ));
                }
                if(subUpdateEntities.isEmpty()) {
                    noSubEntityList.add(id);
                    continue;
                }

                if(options.isUpdateChildrenOnly()) {
                    result.addAll(subUpdateEntities);
                } else {
                    List entityList = Collections.singletonList(id);
                    updateProcessor = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                            , DeleteByIdCommand.getInstance()
                            , dtoClassInfo.isPhysicDelete()
                            , dtoClassInfo
                            , entityList
                            , entityList
                            , this.getUpdateChildrenFirst()
                            , false);
                    updateProcessor.addSubUpdateEntities(subUpdateEntities);
                    result.add(updateProcessor);
                }
            }
        }
        if(!options.isUpdateChildrenOnly() && !noSubEntityList.isEmpty()) {
            updateProcessor = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , DeleteByIdBatchCommand.getInstance()
                    , dtoClassInfo.isPhysicDelete()
                    , dtoClassInfo
                    , noSubEntityList
                    , noSubEntityList
                    , this.getUpdateChildrenFirst()
                    , false);
            result.add(updateProcessor);
        }

        return result;
    }
}
