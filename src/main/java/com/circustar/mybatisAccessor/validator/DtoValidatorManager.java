package com.circustar.mybatisAccessor.validator;

import com.circustar.mybatisAccessor.provider.DefaultDeleteEntityProvider;
import com.circustar.mybatisAccessor.provider.DefaultInsertEntityProvider;
import com.circustar.mybatisAccessor.provider.DefaultUpdateEntityProvider;
import com.circustar.mybatisAccessor.provider.IUpdateEntityProvider;
import com.circustar.mybatisAccessor.utils.ClassUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindingResult;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DtoValidatorManager {
    private Map<Class, Map<IUpdateEntityProvider, IDtoValidator>> validatorMap = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;
    private Map<IUpdateEntityProvider, IDtoValidator> defaultValidatorMap = new HashMap<>();
    private DefaultInsertEntityProvider defaultInsertEntityProvider;
    private DefaultUpdateEntityProvider defaultUpdateEntityProvider;
    private DefaultDeleteEntityProvider defaultDeleteEntityProvider;
    private DefaultInsertValidator defaultInsertValidator;
    private DefaultUpdateValidator defaultUpdateValidator;
    private DefaultDeleteValidator defaultDeleteValidator;
    public DtoValidatorManager(ApplicationContext applicationContext
            , DefaultInsertEntityProvider defaultInsertEntityProvider
            , DefaultUpdateEntityProvider defaultUpdateEntityProvider
            , DefaultDeleteEntityProvider defaultDeleteEntityProvider) {
        this.applicationContext = applicationContext;
        this.defaultInsertEntityProvider = defaultInsertEntityProvider;
        this.defaultUpdateEntityProvider = defaultUpdateEntityProvider;
        this.defaultDeleteEntityProvider = defaultDeleteEntityProvider;
        this.defaultInsertValidator = new DefaultInsertValidator(applicationContext);
        this.defaultUpdateValidator = new DefaultUpdateValidator(applicationContext);
        this.defaultDeleteValidator = new DefaultDeleteValidator(applicationContext);
        defaultValidatorMap.put(defaultInsertEntityProvider
                , this.defaultInsertValidator);
        defaultValidatorMap.put(defaultUpdateEntityProvider
                , this.defaultUpdateValidator);
        defaultValidatorMap.put(defaultDeleteEntityProvider
                , this.defaultDeleteValidator);
    }

    public synchronized void initValidatorMap() {
        Map<String, IDtoValidator> beansOfType = this.applicationContext.getBeansOfType(IDtoValidator.class);
        Collection<IDtoValidator> validators = beansOfType.values();
        for(IDtoValidator validator : validators) {
            List<Type[]> typeArguments = ClassUtils.getTypeArguments(validator.getClass());
            Class clazz = (Class)typeArguments.get(0)[0];
            Map<IUpdateEntityProvider, IDtoValidator> mp = validatorMap.get(clazz);
            if(mp == null) {
                mp = new HashMap<>();
                validatorMap.put(clazz, mp);
            }
            Class providerClass = (Class)typeArguments.get(1)[0];
            IUpdateEntityProvider bean = (IUpdateEntityProvider)applicationContext.getBean(providerClass);
            mp.put(bean, validator);
        }
        for(Class clazz : validatorMap.keySet()) {
            Map<IUpdateEntityProvider, IDtoValidator> providerIDtoValidatorMap = validatorMap.get(clazz);
            if(!providerIDtoValidatorMap.containsKey(defaultInsertEntityProvider)) {
                providerIDtoValidatorMap.put(defaultInsertEntityProvider, this.defaultInsertValidator);
            }
            if(!providerIDtoValidatorMap.containsKey(defaultUpdateEntityProvider)) {
                providerIDtoValidatorMap.put(defaultUpdateEntityProvider, this.defaultUpdateValidator);
            }
            if(!providerIDtoValidatorMap.containsKey(defaultInsertEntityProvider)) {
                providerIDtoValidatorMap.put(defaultDeleteEntityProvider, this.defaultDeleteValidator);
            }
        }

    }
    protected IDtoValidator getDtoValidator(Class clazz, IUpdateEntityProvider updateEntityProvider) {
        if(!validatorMap.containsKey(clazz)) {
            validatorMap.put(clazz, defaultValidatorMap);
        }
        Map<IUpdateEntityProvider, IDtoValidator> iUpdateEntityProviderIDtoValidatorMap = validatorMap.get(clazz);
        if(iUpdateEntityProviderIDtoValidatorMap == null) {
            return null;
        }
        return iUpdateEntityProviderIDtoValidatorMap.get(updateEntityProvider);
    }
    public void validate(Object o, IUpdateEntityProvider updateEntityProvider, BindingResult bindingResult) {
        assert(o != null);
        Class clazz = o.getClass();
        IDtoValidator validator = getDtoValidator(clazz, updateEntityProvider);
        if(validator != null) {
            validator.validate(o, bindingResult);
        }
    }

}
