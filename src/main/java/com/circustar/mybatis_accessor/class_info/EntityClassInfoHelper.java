package com.circustar.mybatis_accessor.class_info;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityClassInfoHelper {
    private static Map<Class<?>, EntityClassInfo> entityClassInfoMap = new ConcurrentHashMap<>();

    public EntityClassInfo getEntityClassInfo(Class<?> clazz) {
        if(entityClassInfoMap.containsKey(clazz)) {
            return entityClassInfoMap.get(clazz);
        }
        EntityClassInfo entityClassInfo = new EntityClassInfo(clazz);
        return tryPut(clazz, entityClassInfo);
    }

    private EntityClassInfo tryPut(Class<?> clazz, EntityClassInfo entityClassInfo) {
        try {
            entityClassInfoMap.put(clazz, entityClassInfo);
        } catch (Exception ex) {
        }
        return entityClassInfoMap.get(clazz);

    }
}
