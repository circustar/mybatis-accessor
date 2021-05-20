package com.circustar.mybatis_accessor.validator;

import com.circustar.mybatis_accessor.provider.DefaultInsertEntityProvider;
import org.springframework.context.ApplicationContext;

public class DefaultInsertValidator extends AbstractDefaultDtoValidator<DefaultInsertEntityProvider> {
    public DefaultInsertValidator(ApplicationContext applicationContext) {
        super(applicationContext);
    }
}
