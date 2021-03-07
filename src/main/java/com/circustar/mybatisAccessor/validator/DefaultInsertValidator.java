package com.circustar.mybatisAccessor.validator;

import com.circustar.mybatisAccessor.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultInsertValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultInsertValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
