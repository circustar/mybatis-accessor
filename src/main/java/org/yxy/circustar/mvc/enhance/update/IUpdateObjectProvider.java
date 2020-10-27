package org.yxy.circustar.mvc.enhance.update;

import org.springframework.validation.BindingResult;

import java.util.List;

public interface IUpdateObjectProvider {
    public <S> List<UpdateEntity> createUpdateEntities(S s);
    public <S> void validateBeforeUpdate(S s, BindingResult bindingResult);
    default void onSuccess() {};
    default void onFail() {};
    default void onEnd() {};
}
