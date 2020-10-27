package org.yxy.circustar.mvc.enhance.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.yxy.circustar.mvc.enhance.update.IUpdateObjectProvider;

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

    public Class<IUpdateObjectProvider> getConverter() {
        return converter;
    }

    public void setConverter(Class<IUpdateObjectProvider> converter) {
        this.converter = converter;
    }

    private Class<? extends  IService> service;
    private Class<IUpdateObjectProvider> converter;

    public EntityDtoServiceRelation(Class dto, Class entity, Class<? extends  IService> service) {
        this.entity = entity;
        this.dto = dto;
        this.service = service;
        this.converter = null;
    }

    public EntityDtoServiceRelation(Class dto, Class entity,  Class<? extends  IService> service, Class<IUpdateObjectProvider> converter) {
        this.entity = entity;
        this.dto = dto;
        this.service = service;
        this.converter = converter;
    }
}
