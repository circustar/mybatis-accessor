package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.IUpdateEntityProvider;
import org.springframework.validation.BindingResult;

import java.util.Collection;
import java.util.Map;

public interface IUpdateService {
    Collection<Object> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object
            , IUpdateEntityProvider updateEntityProviders
            , Map options) throws Exception;
}
