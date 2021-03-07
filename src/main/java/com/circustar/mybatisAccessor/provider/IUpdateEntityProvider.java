package com.circustar.mybatisAccessor.provider;

import com.circustar.mybatisAccessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatisAccessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatisAccessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;

import java.util.List;
import java.util.Map;

public interface IUpdateEntityProvider {
    List<DefaultEntityCollectionUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception;
    default void onSuccess(Object dto, List<Object> updateEntities) {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
