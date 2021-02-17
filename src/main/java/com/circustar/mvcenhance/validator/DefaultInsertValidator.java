package com.circustar.mvcenhance.validator;

import com.circustar.mvcenhance.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultInsertValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultInsertValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
