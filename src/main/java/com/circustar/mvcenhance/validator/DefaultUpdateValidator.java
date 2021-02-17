package com.circustar.mvcenhance.validator;

import com.circustar.mvcenhance.provider.DefaultDeleteEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultUpdateValidator extends AbstractDefaultDtoValidator<DefaultDeleteEntityProvider> {
    public DefaultUpdateValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
