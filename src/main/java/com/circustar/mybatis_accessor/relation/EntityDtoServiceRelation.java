package com.circustar.mybatis_accessor.relation;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import org.springframework.context.ApplicationContext;

public class EntityDtoServiceRelation {
    private Class entityClass;
    private TableInfo tableInfo;

    private Class dtoClass;

    private Class<? extends  IService> serviceClass;

    private IService service = null;

    private DtoClassInfo dtoClassInfo;

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public Class getDtoClass() {
        return dtoClass;
    }

    public void setDtoClass(Class dtoClass) {
        this.dtoClass = dtoClass;
    }

    public Class<? extends IService> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<? extends IService> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public EntityDtoServiceRelation(Class dtoClass, Class entityClass, Class<? extends  IService> serviceClass) {
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
        this.serviceClass = serviceClass;
        this.tableInfo = TableInfoHelper.getTableInfo(entityClass);
    }

    public IService getServiceBean(ApplicationContext applicationContext) {
        if(this.service == null) {
            this.service = applicationContext.getBean(serviceClass);
        }
        return this.service;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public void setDtoClassInfo(DtoClassInfo dtoClassInfo) {
        this.dtoClassInfo = dtoClassInfo;
    }
}
