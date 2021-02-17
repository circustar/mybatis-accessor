package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.service.ISelectService;
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
    protected ISelectService selectService = null;
    protected IEntityDtoServiceRelationMap relationMap = null;

    public IEntityDtoServiceRelationMap getRelationMap(){
        if(this.relationMap != null) {
            return  this.relationMap;
        }
        this.relationMap = applicationContext.getBean(IEntityDtoServiceRelationMap.class);
        return this.relationMap;
    };

    public ISelectService getSelectService(){
        if(this.selectService != null) {
            return  this.selectService;
        }
        this.selectService = applicationContext.getBean(ISelectService.class);
        return this.selectService;
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected String[] getChildren(String[] entities, String prefix, String delimeter) {
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
