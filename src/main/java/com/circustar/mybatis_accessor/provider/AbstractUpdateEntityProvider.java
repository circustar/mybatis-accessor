package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.service.ISelectService;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractUpdateEntityProvider<P extends IProviderParam> implements IUpdateProcessorProvider<P> {
    protected ApplicationContext applicationContext;
    protected ISelectService selectService = null;
    protected static final String DEFAULT_DELIMITER = ".";
    private final static Map<Class, AbstractUpdateEntityProvider> PROVIDER_MAP = new HashMap<>();

    protected Map<Class, AbstractUpdateEntityProvider> getProviderMap() {
        return PROVIDER_MAP;
    }

    public AbstractUpdateEntityProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        PROVIDER_MAP.put(this.getClass(), this);
    }

    public ISelectService getSelectService(){
        if(this.selectService != null) {
            return  this.selectService;
        }
        this.selectService = applicationContext.getBean(ISelectService.class);
        return this.selectService;
    }

    protected List<String> getChildren(List<String> entities, String prefix, String delimiter) {
        if(CollectionUtils.isEmpty(entities)) {
            return null;
        }
        List<String> entityList = entities.stream()
                .filter(x -> StringUtils.hasLength(x) && x.startsWith(prefix + delimiter))
                .map(x -> x.substring((prefix + delimiter).length()))
                .collect(Collectors.toList());
        return entityList;
    }

    protected List<String> getTopEntities(DtoClassInfo dtoClassInfo, List<String> entities, String delimiter) {
        if(entities == null) {
            return Collections.emptyList();
        }
        List<String> entityList = entities.stream()
                .filter(x -> StringUtils.hasLength(x) && !x.contains(delimiter))
                .map(x -> dtoClassInfo.getDtoField(x))
                .sorted(Comparator.comparingInt((DtoField x) -> x.getFieldDtoClassInfo().getUpdateOrder())
                        .thenComparing(x -> x.getFieldDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName()))
                .map(x -> x.getField().getName())
                .collect(Collectors.toList());
        return entityList;
    }

    protected boolean getUpdateChildrenFirst() {
        return false;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
