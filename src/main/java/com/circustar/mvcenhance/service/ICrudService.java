package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.IUpdateTreeProvider;
import org.springframework.validation.BindingResult;

import java.util.Collection;
import java.util.Map;

public interface ICrudService {
    Collection<Object> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object
            , IUpdateTreeProvider updateEntityProviders
            , Map options
            , BindingResult bindingResult) throws Exception;
}
