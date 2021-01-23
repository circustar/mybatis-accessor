package com.circustar.mvcenhance.relation;

public interface IEntityDtoServiceRelationMap {
//    EntityDtoServiceRelation getByEntityClass(Class clazz);
    EntityDtoServiceRelation getByDtoClass(Class clazz);
//    EntityDtoServiceRelation getByEntityName(String name);
    EntityDtoServiceRelation getByDtoName(String name);

    void addRelation(EntityDtoServiceRelation relation);

    Class[] getAllDtoClasses();

    String[] getAllDtoClassNames();
}
