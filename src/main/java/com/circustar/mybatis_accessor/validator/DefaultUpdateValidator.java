package com.circustar.mybatis_accessor.validator;

import com.circustar.mybatis_accessor.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultUpdateValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultUpdateValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
