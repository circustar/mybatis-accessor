package com.circustar.mybatis_accessor.class_info;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityClassInfoHelper {
    protected final static Log LOGGER = LogFactory.getLog(EntityClassInfoHelper.class);
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
            LOGGER.warn(String.format("类%s不能放到entityClassInfoMap中，可能已存在", clazz.getName()));
        }
        return entityClassInfoMap.get(clazz);

    }
}
