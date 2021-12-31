package com.circustar.mybatis_accessor.class_info;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityClassInfoHelper {
    private static Map<Class<?>, EntityClassInfo> entityClassInfoMap = new ConcurrentHashMap<>();

    public EntityClassInfo getEntityClassInfo(Class<?> clazz) {
        if(entityClassInfoMap.containsKey(clazz)) {
            return entityClassInfoMap.get(clazz);
        }
        EntityClassInfo entityClassInfo = new EntityClassInfo(clazz);
        entityClassInfoMap.put(clazz, entityClassInfo);
        return entityClassInfo;
    }
}
