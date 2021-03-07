package com.circustar.mybatisAccessor.service;

import com.circustar.mybatisAccessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatisAccessor.provider.IUpdateEntityProvider;

import java.util.List;
import java.util.Map;

public interface IUpdateService {
    List<Object> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object
            , IUpdateEntityProvider updateEntityProviders
            , Map options) throws Exception;
}
