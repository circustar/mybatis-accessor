package com.circustar.mybatis_accessor.classInfo;

import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.utils.FieldUtils;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DtoClassInfoHelper {
    private static Map<Class<?>, DtoClassInfo> dtoClassInfoMap = new ConcurrentHashMap<>();

    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private EntityClassInfoHelper entityClassInfoHelper;

    public DtoClassInfoHelper(IEntityDtoServiceRelationMap relationMap,EntityClassInfoHelper entityClassInfoHelper) {
        this.entityDtoServiceRelationMap = relationMap;
        this.entityClassInfoHelper = entityClassInfoHelper;
    }
    public DtoClassInfo getDtoClassInfo(Class<?> clazz) {
        if(dtoClassInfoMap.containsKey(clazz)) {
            return dtoClassInfoMap.get(clazz);
        }
        EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(entityDtoServiceRelationMap.getByDtoClass(clazz).getEntityClass());
        DtoClassInfo dtoClassInfo = new DtoClassInfo(entityDtoServiceRelationMap, clazz, entityClassInfo);
        return tryPut(clazz, dtoClassInfo);
    }

    private DtoClassInfo tryPut(Class<?> clazz, DtoClassInfo dtoClassInfo) {
        try {
            dtoClassInfoMap.put(clazz, dtoClassInfo);
        } catch (Exception ex) {
        }
        return dtoClassInfoMap.get(clazz);

    }

    public Object convertToEntity(Object object) throws Exception {
        if(object == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(object.getClass())) {
            return convertToEntityList((Collection)object);
        } else {
            return convertToSingleObject(object);
        }
    }

    public Object convertToSingleObject(Object object) throws Exception {
        DtoClassInfo dtoClassInfo = this.getDtoClassInfo(object.getClass());
        Class targetClass = dtoClassInfo.getEntityDtoServiceRelation().getEntityClass();
        EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(targetClass);

        Object entity = targetClass.newInstance();
        BeanUtils.copyProperties(object, entity);
        for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
            EntityFieldInfo entityEntityFieldInfo = entityClassInfo.getFieldByName(dtoField.getField().getName());
            if(entityEntityFieldInfo == null) {
                continue;
            }
            Object subDto = FieldUtils.getValue(object ,dtoField.getField());
            Object child = convertToEntity(subDto);
            FieldUtils.setField(entity, entityEntityFieldInfo.getField(), child);
        }
        return entity;
    }

    public <T extends Collection> T convertToEntityList(T objects) throws Exception {
        Class collectionClass = objects.getClass();
        Class<? extends Collection> implementClass = CollectionType.getSupportCollectionType(collectionClass);
        if(implementClass == null) {
            throw new Exception("Collection type not Support!");
        }
        Class dtoClass = null;
        DtoClassInfo dtoClassInfo = null;
        Class targetClass = null;
        EntityClassInfo entityClassInfo = null;
        Collection childList =  implementClass.newInstance();
        Iterator it = objects.iterator();
        while(it.hasNext()) {
            Object object = it.next();
            if(object == null) continue;
            if(dtoClass == null) {
                dtoClass = object.getClass();
                dtoClassInfo = this.getDtoClassInfo(dtoClass);
                targetClass = dtoClassInfo.getEntityDtoServiceRelation().getEntityClass();
                entityClassInfo = entityClassInfoHelper.getEntityClassInfo(targetClass);
            }
            Object child = entityClassInfo.getEntityClass().newInstance();
            BeanUtils.copyProperties(object, child);
            childList.add(child);
        }
        if(dtoClass == null) {return (T)childList;}
        for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
            EntityFieldInfo entityEntityFieldInfo = entityClassInfo.getFieldByName(dtoField.getField().getName());
            if(entityEntityFieldInfo == null && !entityEntityFieldInfo.getIsCollection()) {
                continue;
            }
            Iterator itFrom = objects.iterator();
            Iterator itTo = childList.iterator();
            while(itFrom.hasNext()) {
                Object object = FieldUtils.getValue(itFrom.next(), dtoField.getField());
                Object child = convertToEntity(object);
                FieldUtils.setField(itTo.next(), entityEntityFieldInfo.getField(), child);
            }
        }
        return (T) childList;
    }


    public Object convertFromEntity(Object entity, Class<?> targetClass) throws Exception  {
        if(entity == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(entity.getClass())) {
            return convertFromEntityList((Collection)entity, targetClass);
        } else {
            return convertFromSingleObject(entity, targetClass);
        }
    }


    public <T> T convertFromSingleObject(Object entity, Class<T> targetClass) throws Exception  {
        DtoClassInfo dtoClassInfo = this.getDtoClassInfo(targetClass);

        T object = targetClass.newInstance();
        BeanUtils.copyProperties(entity, object);
        for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
            if(dtoField.getEntityFieldInfo() == null) {
                continue;
            }
            EntityFieldInfo entityEntityFieldInfo = dtoClassInfo.getEntityClassInfo().getFieldByName(dtoField.getField().getName());
            Object child = FieldUtils.getValue(entity , entityEntityFieldInfo.getField());
            Object subObject = convertFromEntity(child, (Class)dtoField.getActualType());
            FieldUtils.setField(object, dtoField.getField(), subObject);
        }
        return object;
    }

    public <T extends Collection> T convertFromEntityList(T entityList, Class<?> targetClass) throws Exception {
        Class collectionClass = entityList.getClass();
        Class<T> implementClass = (Class<T>) CollectionType.getSupportCollectionType(collectionClass);
        if(implementClass == null) {
            throw new Exception("Collection type not Support!");
        }
        DtoClassInfo dtoClassInfo = this.getDtoClassInfo(targetClass);
        T objectList =  implementClass.newInstance();
        Iterator it = entityList.iterator();
        while(it.hasNext()) {
            Object entity = it.next();
            if(entity == null) {
                objectList.add(null);
                continue;
            }
            Object object = targetClass.newInstance();
            BeanUtils.copyProperties(entity, object);
            objectList.add(object);
        }
        for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
            if(dtoField.getEntityFieldInfo() == null) {
                continue;
            }
            EntityFieldInfo entityEntityFieldInfo = dtoField.getEntityFieldInfo();
            Iterator itFrom = entityList.iterator();
            Iterator itTo = objectList.iterator();
            while(itFrom.hasNext()) {
                Object entity = itFrom.next();
                if(entity == null) continue;
                Object child = FieldUtils.getValue(entity, entityEntityFieldInfo.getField());
                if(child == null) {
                    continue;
                }
                Object object = convertFromEntity(child, (Class)dtoField.getActualType());
                FieldUtils.setField(itTo.next(), dtoField.getField(), object);
            }
        }
        return objectList;
    }
}