package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.IUpdateEntityProvider;

import java.util.List;
import java.util.Map;

public interface IUpdateService {
    List<Object> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object
            , IUpdateEntityProvider updateEntityProviders
            , Map options) throws Exception;
}
