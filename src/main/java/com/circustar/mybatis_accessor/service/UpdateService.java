package com.circustar.mybatis_accessor.service;

import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.provider.*;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public <T> List<T> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object, IUpdateEntityProvider provider
            , Map options) throws Exception {
        List<Object> updatedObjects = new ArrayList<>();
        try {
            List<IEntityUpdateProcessor> objList = provider.createUpdateEntities(relationInfo, dtoClassInfoHelper
                    , object, options);
            for(IEntityUpdateProcessor o : objList) {
                boolean result = o.execUpdate();
                if(!result) {
                    throw new UpdateTargetNotFoundException("update failed");
                }
                updatedObjects.addAll(o.getUpdateTargets());
            }
            provider.onSuccess(object, updatedObjects);
        } catch (Exception ex) {
            provider.onException(ex);
            throw ex;
        } finally {
            provider.onEnd();
        }

        if(updatedObjects.size() == 0) {
            return (List<T>) updatedObjects;
        }
        return (List<T>) dtoClassInfoHelper.convertFromEntityList(updatedObjects, relationInfo.getDtoClass());
    }
}
