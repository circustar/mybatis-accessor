package com.circustar.mybatisAccessor.validator;

import com.circustar.mybatisAccessor.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultUpdateValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultUpdateValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
