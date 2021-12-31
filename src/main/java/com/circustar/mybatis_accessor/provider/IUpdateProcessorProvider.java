package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.update_processor.IEntityUpdateProcessor;

import java.util.List;

public interface IUpdateProcessorProvider<P extends IProviderParam> {
     List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, P options);
    default <T> void onSuccess(Object dto, List<T> updateEntities) {};
}
