package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

public abstract class AbstractUpdateEntityProvider implements IUpdateEntityProvider, ApplicationContextAware {
    protected ApplicationContext applicationContext;

    public EnhancedConversionService getConversionService() {
        return applicationContext.getBean(EnhancedConversionService.class);
    };
    public IEntityDtoServiceRelationMap getRelationMap(){
        return applicationContext.getBean(IEntityDtoServiceRelationMap.class);
    };
    public Validator getValidator(){
        try {
            return applicationContext.getBean(SmartValidator.class);
        } catch (Exception ex) {
        }
        return null;
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    @Override
    public void validateAndSet(Object s, BindingResult bindingResult, Object... options){
        if(s == null) {
            return;
        }
        Validator v = getValidator();
        if(v != null) {
            v.validate(s, bindingResult);
        }
    };
}
