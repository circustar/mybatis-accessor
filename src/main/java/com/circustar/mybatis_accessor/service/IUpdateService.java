package com.circustar.mybatis_accessor.service;

import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.IUpdateProcessorProvider;

import java.util.List;

public interface IUpdateService {
    <T> List<T> updateByProviders(EntityDtoServiceRelation relationInfo
            , Object object
            , IUpdateProcessorProvider updateEntityProviders
            , IProviderParam options);
}
