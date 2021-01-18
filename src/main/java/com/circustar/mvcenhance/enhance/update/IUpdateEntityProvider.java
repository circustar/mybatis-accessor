package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IUpdateEntityProvider {
    Collection<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object obj, Map options) throws Exception;
    default <S> void validateAndSet(Object obj, BindingResult bindingResult, Map options){};
    default void onSuccess() {};
    default void onException(Exception ex) {}
    default void onEnd() {};
}
