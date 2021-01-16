package com.circustar.mvcenhance.enhance.service;

import com.circustar.mvcenhance.common.error.UpdateTargetNotFoundException;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.update.*;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<Object> updateByProviders(EntityDtoServiceRelation relationInfo, Object object, String[] updateNames, Map options, BindingResult bindingResult) throws Exception {
        List<Object> updatedObjects = new ArrayList<>();
        List<? extends IUpdateEntityProvider> providers = Arrays.stream(relationInfo.getUpdateObjectProviders())
                .map(x -> this.applicationContext.getBean(x))
                .filter(x -> x.match(updateNames))
                .collect(Collectors.toList());
        for(IUpdateEntityProvider provider : providers) {
            provider.validateAndSet(object, bindingResult, options);
            if(bindingResult.hasErrors()) {
                throw new ValidateException("validate failed");
            }
            try {
                List<UpdateEntity> objList = provider.createUpdateEntities(relationInfo, dtoClassInfoHelper, object, options);
                for(UpdateEntity o : objList) {
                    boolean result = o.execUpdate(UpdateEntity.DEFAULT_BATCH_LIMIT);
                    if(!result) {
                        throw new UpdateTargetNotFoundException("update failed");
                    }
                    updatedObjects.addAll(o.getUpdatedEntityList());
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

        return dtoClassInfoHelper.convertFromEntityList(updatedObjects, relationInfo.getDto());
    }
}
