package com.circustar.mybatis_accessor.validator;

import com.circustar.mybatis_accessor.provider.IUpdateEntityProvider;
import org.springframework.validation.BindingResult;

public interface IDtoValidator<clazz extends Class, PROVIDER extends IUpdateEntityProvider> {
    void validate(Object dto, BindingResult bindingResult);
}
