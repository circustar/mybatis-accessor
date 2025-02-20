package com.circustar.mybatis_accessor.relation;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.converter.IConverter;
import com.circustar.mybatis_accessor.utils.ApplicationContextUtils;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class EntityDtoServiceRelation {
    private Class entityClass;
    private final TableInfo tableInfo;

    private Class dtoClass;

    private String name;

    private Class<? extends  IService> serviceClass;

    private IService service;

    private final Class<? extends IConverter> convertDtoToEntityClass;

    private final Class<? extends IConverter> convertEntityToDtoClass;

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

    public EntityDtoServiceRelation(Class dtoClass, Class entityClass
            , String name, Class<? extends  IService> serviceClass
            , Class<? extends IConverter> convertDtoToEntityClass, Class<? extends IConverter> convertEntityToDtoClass) {
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
        this.name = name;
        this.serviceClass = serviceClass;
        this.convertDtoToEntityClass = convertDtoToEntityClass;
        this.convertEntityToDtoClass = convertEntityToDtoClass;
        this.tableInfo = TableInfoHelper.getTableInfo(entityClass);
    }

    public IService getServiceBean(ApplicationContext applicationContext) {
        if(this.service == null) {
            this.service = ApplicationContextUtils.getAnyBean(applicationContext, serviceClass);
        }
        if(this.service == null) {
            final Collection<?> values = applicationContext.getBeansOfType(IService.class).values();
            for(Object obj : values) {
                if(serviceClass.isAssignableFrom(obj.getClass())
                        ||obj.getClass().getName().startsWith(serviceClass.getName() + "$")
                        || Arrays.stream(obj.getClass().getSuperclass().getInterfaces()).anyMatch( x-> x.getName().equals(serviceClass.getName()))) {
                    this.service = (IService) obj;
                    break;
                }
            }
        }
        return this.service;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public void setDtoClassInfo(DtoClassInfo dtoClassInfo) {
        this.dtoClassInfo = dtoClassInfo;
    }

    public Class<? extends IConverter> getConvertDtoToEntityClass() {
        return convertDtoToEntityClass;
    }

    public Class<? extends IConverter> getConvertEntityToDtoClass() {
        return convertEntityToDtoClass;
    }

    public String getName() {
        return name;
    }
}
