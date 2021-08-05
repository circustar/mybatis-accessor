package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.service.ISelectService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
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
        return entityList.toArray(new String[0]);
    }

    protected String[] getTopEntities(String[] entities, String delimeter) {
        List<String> entityList = Arrays.stream(entities)
                .filter(x -> !StringUtils.isEmpty(x) && !x.contains(delimeter))
                .collect(Collectors.toList());
        return entityList.toArray(new String[0]);
    }

    protected boolean getPhysicDelete(DtoClassInfoHelper dtoClassInfoHelper, EntityDtoServiceRelation relation) {
        boolean physicDelete = false;
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        if(dtoClassInfo.getDeleteFlagField() != null) {
            physicDelete = dtoClassInfo.isPhysicDelete();
        }
        return physicDelete;
    }
}
