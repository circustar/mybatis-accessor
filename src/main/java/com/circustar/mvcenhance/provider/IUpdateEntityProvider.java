package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.updateProcessor.DefaultEntityCollectionUpdateProcessor;

import java.util.List;
import java.util.Map;

public interface IUpdateEntityProvider {
    List<DefaultEntityCollectionUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception;
    default void onSuccess(Object dto, List<Object> updateEntities) {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
