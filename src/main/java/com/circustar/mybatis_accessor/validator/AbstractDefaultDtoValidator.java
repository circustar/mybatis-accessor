package com.circustar.mybatis_accessor.validator;

import com.circustar.mybatis_accessor.provider.IUpdateEntityProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractDefaultDtoValidator<PROVIDER extends IUpdateEntityProvider> implements IDtoValidator<Class<Object>, PROVIDER> {
    protected ApplicationContext applicationContext;
    public AbstractDefaultDtoValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected Validator validator = null;
    protected volatile boolean initFlag = false;

    protected synchronized void initDefaultValidator() {
        if(initFlag) {
            return;
        }
        initFlag = true;
        Map<String, SpringValidatorAdapter> beans1 = applicationContext.getBeansOfType(SpringValidatorAdapter.class);
        if(beans1 != null) {
            Collection<SpringValidatorAdapter> values = beans1.values();
            if(values.size() > 0) {
                this.validator = values.iterator().next();
                return;
            }
        }

        Map<String, SmartValidator> beans2 = applicationContext.getBeansOfType(SmartValidator.class);
        if(beans2 != null) {
            Collection<SmartValidator> values = beans2.values();
            if(values.size() > 0) {
                this.validator = values.iterator().next();
                return;
            }
        }

        Map<String, Validator> beans3 = applicationContext.getBeansOfType(Validator.class);
        if(beans3 != null) {
            Collection<Validator> values = beans3.values();
            if(values.size() > 0) {
                this.validator = values.iterator().next();
            }
        }
    }

    @Override
    public void validate(Object o, BindingResult bindingResult) {
        initDefaultValidator();
        if(this.validator != null) {
            this.validator.validate(o, bindingResult);
        }
    }
}
