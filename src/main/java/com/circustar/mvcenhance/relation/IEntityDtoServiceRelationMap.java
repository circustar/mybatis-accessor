package com.circustar.mvcenhance.relation;

public interface IEntityDtoServiceRelationMap {

    EntityDtoServiceRelation getByDtoClass(Class clazz);

    EntityDtoServiceRelation getByDtoName(String name);

    void addRelation(EntityDtoServiceRelation relation);

    Class[] getAllDtoClasses();

    String[] getAllDtoClassNames();
}
