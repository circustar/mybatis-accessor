package com.circustar.mybatis_accessor.validator;

import com.circustar.mybatis_accessor.provider.DefaultUpdateEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultUpdateValidator extends AbstractDefaultDtoValidator<DefaultUpdateEntityProvider> {
    public DefaultUpdateValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
 