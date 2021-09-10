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
    protected IEntityDtoServiceRelationMap relationMap = null;
    protected DtoClassInfoHelper dtoClassInfoHelper = null;
    private static final String[] emptyArray = new String[0];

    public DtoClassInfoHelper getDtoClassInfoHelper(){
        if(this.dtoClassInfoHelper != null) {
            return  this.dtoClassInfoHelper;
        }
        this.dtoClassInfoHelper = applicationContext.getBean(DtoClassInfoHelper.class);
        return this.dtoClassInfoHelper;
    };

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
                .filter(x -> StringUtils.hasLength(x) && x.startsWith(prefix + delimeter))
                .map(x -> x.substring((prefix + delimeter).length()))
                .collect(Collectors.toList());
        return entityList.toArray(new String[0]);
    }

    protected String[] getTopEntities(DtoClassInfo dtoClassInfo, String[] entities) {
        if(entities == null) {
            return emptyArray;
        }
        List<String> entityList = Arrays.stream(entities)
                .filter(x -> StringUtils.hasLength(x))
                .map(x -> dtoClassInfo.getDtoField(x))
                .sorted(Comparator.comparingInt((DtoField x) -> x.getFieldDtoClassInfo(getDtoClassInfoHelper()).getUpdateOrder())
                        .thenComparing(x -> x.getFieldDtoClassInfo(getDtoClassInfoHelper()).getEntityClassInfo().getEntityClass().getSimpleName()))
                .map(x -> x.getField().getName())
                .collect(Collectors.toList());
        return entityList.toArray(new String[0]);
    }
}
