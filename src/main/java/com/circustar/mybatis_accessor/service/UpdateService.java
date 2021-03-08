package com.circustar.mybatis_accessor.service;

import com.circustar.mybatis_accessor.error.UpdateTargetNotFoundException;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.provider.*;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
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
    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object, IUpdateEntityProvider provider
            , Map options) throws Exception {
        List<Object> updatedObjects = new ArrayList<>();
        try {
            List<DefaultEntityCollectionUpdateProcessor> objList = provider.createUpdateEntities(relationInfo, dtoClassInfoHelper
                    , object, options);
            for(DefaultEntityCollectionUpdateProcessor o : objList) {
                boolean result = o.execUpdate();
                if(!result) {
                    throw new UpdateTargetNotFoundException("update failed");
                }
                updatedObjects.addAll(o.getUpdateEntities());
            }
            provider.onSuccess(object, updatedObjects);
        } catch (Exception ex) {
            provider.onException(ex);
            throw ex;
        } finally {
            try {
                provider.onEnd();
            } catch (Exception ex) {
                provider.onException(ex);
                throw ex;
            }
        }

        if(updatedObjects.size() == 0) {
            return (List<T>) updatedObjects;
        }
        return (List<T>) dtoClassInfoHelper.convertFromEntityList(updatedObjects, relationInfo.getDtoClass());
    }
}
