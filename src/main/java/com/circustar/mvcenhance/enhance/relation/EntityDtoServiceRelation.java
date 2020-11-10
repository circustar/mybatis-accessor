package com.circustar.mvcenhance.enhance.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.update.IUpdateObjectProvider;

public class EntityDtoServiceRelation {
    private Class entity;
    private Class dto;

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

    public Class<IUpdateObjectProvider> getUpdateObjectProvider() {
        return updateObjectProvider;
    }

    public void setUpdateObjectProvider(Class<IUpdateObjectProvider> updateObjectProvider) {
        this.updateObjectProvider = updateObjectProvider;
    }

    private Class<? extends  IService> service;
    private Class<IUpdateObjectProvider> updateObjectProvider;

    public EntityDtoServiceRelation(Class dto, Class entity, Class<? extends  IService> service) {
        this.entity = entity;
        this.dto = dto;
        this.service = service;
        this.updateObjectProvider = null;
    }

    public EntityDtoServiceRelation(Class dto, Class entity,  Class<? extends  IService> service, Class<IUpdateObjectProvider> updateObjectProvider) {
        this.entity = entity;
        this.dto = dto;
        this.service = service;
        this.updateObjectProvider = updateObjectProvider;
    }
}
