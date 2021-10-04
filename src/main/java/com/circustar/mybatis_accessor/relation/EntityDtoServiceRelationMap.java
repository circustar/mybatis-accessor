package com.circustar.mybatis_accessor.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDtoServiceRelationMap implements IEntityDtoServiceRelationMap {
    private List<EntityDtoServiceRelation> entityDtoServiceRelationList = new ArrayList<>();

    private Map<Class, EntityDtoServiceRelation> dtoMap = new HashMap<>();

    private Map<String, EntityDtoServiceRelation> dtoNameMap = new HashMap<>();

    private ApplicationContext applicationContext;

    public EntityDtoServiceRelationMap(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
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

    @Override
    public IService getServiceBeanByDtoClass(Class clazz) {
        return getByDtoClass(clazz).getServiceBean(this.applicationContext);
    }
}
