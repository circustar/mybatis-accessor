package com.circustar.mybatis_accessor.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.context.ApplicationContext;

public interface IEntityDtoServiceRelationMap {

    EntityDtoServiceRelation getByDtoClass(Class clazz);

    EntityDtoServiceRelation getByDtoName(String name);

    void addRelation(EntityDtoServiceRelation relation);

    Class[] getAllDtoClasses();

    String[] getAllDtoClassNames();

    IService getServiceBeanByDtoClass(Class clazz);

    ApplicationContext getApplicationContext();
}
