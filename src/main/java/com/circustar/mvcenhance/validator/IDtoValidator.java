package com.circustar.mvcenhance.validator;

import com.circustar.mvcenhance.provider.IUpdateEntityProvider;
import org.springframework.validation.BindingResult;

public interface IDtoValidator<clazz extends Class, PROVIDER extends IUpdateEntityProvider> {
    void validate(Object dto, BindingResult bindingResult);
}
