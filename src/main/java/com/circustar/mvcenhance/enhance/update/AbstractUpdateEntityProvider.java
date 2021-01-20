package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractUpdateEntityProvider implements IUpdateEntityProvider, ApplicationContextAware {
    protected ApplicationContext applicationContext;

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
    public void validateAndSet(Object s, BindingResult bindingResult, Map options){
        if(s == null) {
            return;
        }
        Validator v = getValidator();
        if(v != null) {
            v.validate(s, bindingResult);
        }
    };

    protected String[] getSubEntities(String[] entities, String prefix, String delimeter) {
        List<String> entityList = Arrays.stream(entities)
                .filter(x -> !StringUtils.isEmpty(x) && x.startsWith(prefix + delimeter))
                .map(x -> x.substring((prefix + delimeter).length()))
                .collect(Collectors.toList());
        return entityList.toArray(new String[entityList.size()]);
    }

    protected String[] getTopEntities(String[] entities, String delimeter) {
        List<String> entityList = Arrays.stream(entities)
                .filter(x -> !StringUtils.isEmpty(x) && !x.contains(delimeter))
                .collect(Collectors.toList());
        return entityList.toArray(new String[entityList.size()]);
    }
}
