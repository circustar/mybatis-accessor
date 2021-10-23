package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;

import java.util.List;

public interface IUpdateProcessorProvider<P extends IProviderParam> {
     List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, P options);
    default <T> void onSuccess(Object dto, List<T> updateEntities) {};
}
