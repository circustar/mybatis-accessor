package com.circustar.mybatis_accessor.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityDtoServiceRelationMap implements IEntityDtoServiceRelationMap {
    private final List<EntityDtoServiceRelation> entityDtoServiceRelationList = new ArrayList<>();

    private final Map<Class, EntityDtoServiceRelation> dtoMap = new ConcurrentHashMap<>();

    private final Map<String, EntityDtoServiceRelation> dtoNameMap = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;

    public EntityDtoServiceRelationMap(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    @Override
    public void addRelation(EntityDtoServiceRelation relation) {
        entityDtoServiceRelationList.add(relation);
        final Class dtoClass = relation.getDtoClass();
        dtoMap.put(dtoClass, relation);
        dtoNameMap.put(dtoClass.getName(), relation);
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
            dtoNameMap.put(StringUtils.hasLength(x.getName()) ? x.getName()
                    : x.getDtoClass().getSimpleName(), x);
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

    @Override
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }
}
