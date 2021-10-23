package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.service.ISelectService;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractUpdateEntityProvider<P extends IProviderParam> implements IUpdateProcessorProvider<P> {
    protected ApplicationContext applicationContext;
    protected ISelectService selectService = null;
    private static final String[] EMPTY_ARRAY = new String[0];
    protected static final String DEFAULT_DELIMITER = ".";
    protected final static Map<Class, AbstractUpdateEntityProvider> PROVIDER_MAP = new HashMap<>();

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

    protected String[] getChildren(String[] entities, String prefix, String delimiter) {
        List<String> entityList = Arrays.stream(entities)
                .filter(x -> StringUtils.hasLength(x) && x.startsWith(prefix + delimiter))
                .map(x -> x.substring((prefix + delimiter).length()))
                .collect(Collectors.toList());
        return entityList.toArray(new String[0]);
    }

    protected String[] getTopEntities(DtoClassInfo dtoClassInfo, String[] entities, String delimiter) {
        if(entities == null) {
            return EMPTY_ARRAY;
        }
        List<String> entityList = Arrays.stream(entities)
                .filter(x -> StringUtils.hasLength(x) && !x.contains(delimiter))
                .map(x -> dtoClassInfo.getDtoField(x))
                .sorted(Comparator.comparingInt((DtoField x) -> x.getFieldDtoClassInfo().getUpdateOrder())
                        .thenComparing(x -> x.getFieldDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName()))
                .map(x -> x.getField().getName())
                .collect(Collectors.toList());
        return entityList.toArray(new String[0]);
    }

    protected boolean getUpdateChildrenFirst() {
        return false;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
