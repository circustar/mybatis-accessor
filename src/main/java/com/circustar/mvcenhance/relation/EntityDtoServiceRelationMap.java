package com.circustar.mvcenhance.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDtoServiceRelationMap implements IEntityDtoServiceRelationMap {
    private List<EntityDtoServiceRelation> entityDtoServiceRelationList = new ArrayList<>();

    private Map<Class, EntityDtoServiceRelation> dtoMap = new HashMap<>();

    private Map<String, EntityDtoServiceRelation> dtoNameMap = new HashMap<>();

    public EntityDtoServiceRelationMap(){
    }

    public void addRelation(EntityDtoServiceRelation relation) {
        entityDtoServiceRelationList.add(relation);
        dtoMap.put(relation.getDtoClass(), relation);
        dtoNameMap.put(relation.getDtoClass().getSimpleName(), relation);
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
            dtoMap.put(x.getDtoClass(), x);
            dtoNameMap.put(x.getDtoClass().getSimpleName(), x);
        });
    };

    @Override
    public EntityDtoServiceRelation getByDtoClass(Class entityClass) {
        return dtoMap.get(entityClass);
    }

    @Override
    public EntityDtoServiceRelation getByDtoName(String dtoName) {
        return dtoNameMap.get(dtoName);
    }
}
