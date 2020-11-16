package com.circustar.mvcenhance.enhance.relation;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.update.IUpdateEntityProvider;

public class EntityDtoServiceRelation {
    private Class entity;
    private TableInfo tableInfo;

    private Class dto;
    private DtoClassInfo dtoClassInfo;

    private Class<? extends  IService> service;
    private Class<? extends IUpdateEntityProvider>[] updateObjectProviders;

    public Class getEntity() {
        return entity;
    }

    public void setEntity(Class entity) {
        this.entity = entity;
    }

    public Class getDto() {
        return dto;
    }

    public void setDto(Class dto) {
        this.dto = dto;
    }

    public Class<? extends IService> getService() {
        return service;
    }

    public void setService(Class<? extends IService> service) {
        this.service = service;
    }

    public Class<? extends IUpdateEntityProvider>[] getUpdateObjectProviders() {
        return updateObjectProviders;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public void setDtoClassInfo(DtoClassInfo dtoClassInfo) {
        this.dtoClassInfo = dtoClassInfo;
    }

    public void setUpdateObjectProviders(Class<? extends IUpdateEntityProvider>[] updateObjectProviders) {
        this.updateObjectProviders = updateObjectProviders;
    }

    public EntityDtoServiceRelation(Class dto, Class entity, Class<? extends  IService> service) {
        this(dto, entity, service, null);
    }

    public EntityDtoServiceRelation(Class dto, Class entity, Class<? extends  IService> service, Class<? extends IUpdateEntityProvider>[] updateObjectProviders) {
        this.entity = entity;
        this.dto = dto;
        this.service = service;
        this.updateObjectProviders = updateObjectProviders;
        this.tableInfo = TableInfoHelper.getTableInfo(entity);
        this.dtoClassInfo = new DtoClassInfo(dto, entity);
    }
}
