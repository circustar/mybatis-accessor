package com.circustar.mybatis_accessor.validator;

import com.circustar.mybatis_accessor.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultInsertValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultInsertValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
