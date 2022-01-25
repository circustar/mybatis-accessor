package com.circustar.mybatis_accessor.service;

import com.circustar.mybatis_accessor.class_info.*;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.provider.*;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.update_processor.IEntityUpdateProcessor;


import java.util.*;

public class UpdateService implements IUpdateService {
    private final DtoClassInfoHelper dtoClassInfoHelper;

    public UpdateService(DtoClassInfoHelper dtoClassInfoHelper) {
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }

    @Override
    public <T> List<T> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object, IUpdateProcessorProvider provider
            , IProviderParam options
            , String updateEventLogId) throws MybatisAccessorException {
        List<T> updatedObjects = new ArrayList<>();
        List<IEntityUpdateProcessor> objList = provider.createUpdateEntities(relationInfo, dtoClassInfoHelper
                , object, options);
        for(IEntityUpdateProcessor o : objList) {
            boolean result = o.execUpdate(updateEventLogId);
            if(!result) {
                throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                        , String.format(MvcEnhanceConstants.UPDATE_TARGET_NOT_FOUND
                        , "DTO CLASS - " + relationInfo.getDtoClass().getSimpleName()
                                + ", UPDATE PROCESSOR - " + o.getClass().getSimpleName()));
            }
            if(o.getUpdatedEntityList() != null && o.getUpdatedEntityList().size() > 0) {
                updatedObjects.addAll(o.getUpdatedEntityList());
            }
        }
        provider.onSuccess(object, updatedObjects);

        return updatedObjects;
    }
}
