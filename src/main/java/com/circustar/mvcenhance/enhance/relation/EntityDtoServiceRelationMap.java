package com.circustar.mvcenhance.enhance.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDtoServiceRelationMap implements IEntityDtoServiceRelationMap {
    private List<EntityDtoServiceRelation> entityDtoServiceRelationList = new ArrayList<>();
//    private Map<Class, EntityDtoServiceRelation> entityMap = new HashMap<>();
    private Map<Class, EntityDtoServiceRelation> dtoMap = new HashMap<>();
//    private Map<String, EntityDtoServiceRelation> entityNameMap = new HashMap<>();
    private Map<String, EntityDtoServiceRelation> dtoNameMap = new HashMap<>();

    public EntityDtoServiceRelationMap(){
    }

    public void addRelation(EntityDtoServiceRelation relation) {
        entityDtoServiceRelationList.add(relation);
        dtoMap.put(relation.getDto(), relation);
//            entityNameMap.put(x.getEntity().getSimpleName(), x);
        dtoNameMap.put(relation.getDto().getSimpleName(), relation);
    }

    @Override
    public Class[] getAllDtoClasses() {
        return dtoMap.keySet().toArray(new Class[0]);
    }

    @Override
    public String[] getAllDtoClassNames() {
        return dtoNameMap.keySet().toArray(new String[0]);
    }

    public void resetMap() {
        dtoMap.clear();
        dtoNameMap.clear();
        entityDtoServiceRelationList.stream().forEach(x -> {
//            entityMap.put(x.getEntity(), x);
            dtoMap.put(x.getDto(), x);
//            entityNameMap.put(x.getEntity().getSimpleName(), x);
            dtoNameMap.put(x.getDto().getSimpleName(), x);
        });
    };

//    @Override
//    public EntityDtoServiceRelation getByEntityClass(Class entityClass) {
//
//        return entityMap.get(entityClass);
//    }
//
//    @Override
//    public EntityDtoServiceRelation getByEntityName(String entityName) {
//        return entityNameMap.get(entityName);
//    }

    @Override
    public EntityDtoServiceRelation getByDtoClass(Class entityClass) {
        return dtoMap.get(entityClass);
    }

    @Override
    public EntityDtoServiceRelation getByDtoName(String dtoName) {
        return dtoNameMap.get(dtoName);
    }
}
