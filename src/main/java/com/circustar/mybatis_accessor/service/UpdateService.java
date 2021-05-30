package com.circustar.mybatis_accessor.service;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.common.MessageProperties;
import com.circustar.mybatis_accessor.provider.*;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import java.util.*;

public class UpdateService implements IUpdateService {
    public UpdateService(ApplicationContext applicationContext, DtoClassInfoHelper dtoClassInfoHelper, IEntityDtoServiceRelationMap entityDtoServiceRelationMap) {
        this.applicationContext = applicationContext;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
    }
    private ApplicationContext applicationContext;

    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private DtoClassInfoHelper dtoClassInfoHelper;

    @Override
    public <T> List<T> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object, IUpdateEntityProvider provider
            , Map options) {
        List<Object> updatedObjects = new ArrayList<>();
        List<IEntityUpdateProcessor> objList = provider.createUpdateEntities(relationInfo, dtoClassInfoHelper
                , object, options);
        for(IEntityUpdateProcessor o : objList) {
            boolean result = o.execUpdate();
            if(!result) {
                throw new RuntimeException(String.format(MessageProperties.UPDATE_TARGET_NOT_FOUND
                        , "DTO CLASS - " + relationInfo.getDtoClass().getSimpleName()
                                + ", UPDATE PROCESSOR - " + o.getClass().getSimpleName()));
            }
            updatedObjects.addAll(o.getUpdateTargets());
        }
        provider.onSuccess(object, updatedObjects);

        if(updatedObjects.size() == 0) {
            return (List<T>) updatedObjects;
        }
        return (List<T>) dtoClassInfoHelper.convertFromEntityList(updatedObjects, relationInfo.getDtoClass());
    }
}
