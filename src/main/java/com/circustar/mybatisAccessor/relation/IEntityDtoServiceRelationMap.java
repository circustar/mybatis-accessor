package com.circustar.mybatisAccessor.relation;

public interface IEntityDtoServiceRelationMap {

    EntityDtoServiceRelation getByDtoClass(Class clazz);

    EntityDtoServiceRelation getByDtoName(String name);

    void addRelation(EntityDtoServiceRelation relation);

    Class[] getAllDtoClasses();

    String[] getAllDtoClassNames();
}
