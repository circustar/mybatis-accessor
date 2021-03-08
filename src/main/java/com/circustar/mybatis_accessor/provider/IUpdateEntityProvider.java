package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;

import java.util.List;
import java.util.Map;

public interface IUpdateEntityProvider {
    List<DefaultEntityCollectionUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception;
    default <T> void onSuccess(Object dto, List<T> updateEntities) {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
