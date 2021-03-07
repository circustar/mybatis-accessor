package com.circustar.mybatis_accessor.relation;

public interface IEntityDtoServiceRelationMap {

    EntityDtoServiceRelation getByDtoClass(Class clazz);

    EntityDtoServiceRelation getByDtoName(String name);

    void addRelation(EntityDtoServiceRelation relation);

    Class[] getAllDtoClasses();

    String[] getAllDtoClassNames();
}
