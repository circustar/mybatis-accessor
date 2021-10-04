package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.service.ISelectService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractUpdateEntityProvider<P extends IProviderParam> implements IUpdateEntityProvider<P>, ApplicationContextAware {
    protected ApplicationContext applicationContext;
    protected ISelectService selectService = null;
    private static final String[] EMPTY_ARRAY = new String[0];
    protected static final String DEFAULT_DELIMITER = ".";

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
}
