package com.circustar.mybatisAccessor.validator;

import com.circustar.mybatisAccessor.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindingResult;

public class DefaultDeleteValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultDeleteValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
    @Override
    public void validate(Object dto, BindingResult bindingResult) {

    }
}
