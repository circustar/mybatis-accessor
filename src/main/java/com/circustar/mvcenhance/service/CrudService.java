package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.error.UpdateTargetNotFoundException;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.provider.*;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.error.ValidateException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.*;

public class CrudService implements ICrudService {
    public CrudService(ApplicationContext applicationContext, DtoClassInfoHelper dtoClassInfoHelper, IEntityDtoServiceRelationMap entityDtoServiceRelationMap) {
        this.applicationContext = applicationContext;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
    }
    private ApplicationContext applicationContext;

    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private DtoClassInfoHelper dtoClassInfoHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Collection<Object> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object, IUpdateTreeProvider[] updateEntityProviders
            , Map options, boolean returnUpdateResult, BindingResult bindingResult) throws Exception {
        List<Object> updatedObjects = new ArrayList<>();
        for(IUpdateTreeProvider provider : updateEntityProviders) {
            provider.validateAndSet(object, bindingResult, options);
            if (bindingResult.hasErrors()) {
                throw new ValidateException("validate failed");
            }
        }
        for(IUpdateTreeProvider provider : updateEntityProviders) {
            try {
                Collection<UpdateTree> objList = provider.createUpdateEntities(relationInfo, dtoClassInfoHelper
                        , object, options);
                for(UpdateTree o : objList) {
                    boolean result = o.execUpdate();
                    if(!result) {
                        throw new UpdateTargetNotFoundException("update failed");
                    }
                    updatedObjects.addAll(o.getUpdateEntities());
                }
                provider.onSuccess();
            } catch (Exception ex) {
                provider.onException(ex);
                throw ex;
            } finally {
                try {
                    provider.onEnd();
                } catch (Exception ex) {
                }
            }
        }
        if(!returnUpdateResult) {
            return null;
        }

        return dtoClassInfoHelper.convertFromEntityList(updatedObjects, relationInfo.getDto());
    }
}