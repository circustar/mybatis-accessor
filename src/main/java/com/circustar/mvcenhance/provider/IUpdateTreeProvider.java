package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import org.springframework.validation.BindingResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IUpdateTreeProvider {
    Collection<UpdateTree> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object obj, Map options) throws Exception;
    default <S> void validateAndSet(Object obj, BindingResult bindingResult, Map options){};
    default void onSuccess(Object dto, List<Object> updateEntities) {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
