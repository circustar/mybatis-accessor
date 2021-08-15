package com.circustar.mybatis_accessor.service;

import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.IUpdateEntityProvider;

import java.util.List;
import java.util.Map;

public interface IUpdateService {
    <T> List<T> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object
            , IUpdateEntityProvider updateEntityProviders
            , IProviderParam options);
}
