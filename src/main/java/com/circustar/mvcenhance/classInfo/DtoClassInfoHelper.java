package com.circustar.mvcenhance.classInfo;

import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.utils.FieldUtils;
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

    public Object convertToEntity(Object object) {
        if(object == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(object.getClass())) {
            return convertToEntityList((Collection)object);
        } else {
            return convertToSingleObject(object);
        }
    }

    public Object convertToSingleObject(Object object) {
        try {
            DtoClassInfo dtoClassInfo = this.getDtoClassInfo(object.getClass());
            Class targetClass = dtoClassInfo.getEntityDtoServiceRelation().getEntityClass();
            EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(targetClass);

            Object entity = targetClass.newInstance();
            BeanUtils.copyProperties(object, entity);
            for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                TableFieldInfo entityTableFieldInfo = entityClassInfo.getFieldByClass(dtoField.getEntityDtoServiceRelation().getEntityClass());
                if(entityTableFieldInfo == null) {
                    continue;
                }
                Object subDto = FieldUtils.getValue(object ,dtoField.getTableFieldInfo().getField());
                Object child = convertToEntity(subDto);
                FieldUtils.setField(entity, entityTableFieldInfo.getField(), child);
            }
            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends Collection> T convertToEntityList(T objects) {
        try {
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
                Object child = entityClassInfo.getClazz().newInstance();
                BeanUtils.copyProperties(object, child);
                childList.add(child);
            }
            if(dtoClass == null) {return (T)childList;}
            for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                TableFieldInfo entityTableFieldInfo = entityClassInfo.getFieldByClass(dtoField.getEntityDtoServiceRelation().getEntityClass());
                if(entityTableFieldInfo == null && !entityTableFieldInfo.getIsCollection()) {
                    continue;
                }
                Iterator itFrom = objects.iterator();
                Iterator itTo = childList.iterator();
                while(itFrom.hasNext()) {
                    Object object = FieldUtils.getValue(itFrom.next(), dtoField.getTableFieldInfo().getField());
                    Object child = convertToEntity(object);
                    FieldUtils.setField(itTo.next(), entityTableFieldInfo.getField(), child);
                }
            }
            return (T) childList;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public Object convertFromEntity(Object entity, Class<?> targetClass) {
        if(entity == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(entity.getClass())) {
            return convertFromEntityList((Collection)entity, targetClass);
        } else {
            return convertFromSingleObject(entity, targetClass);
        }
    }


    public <T> T convertFromSingleObject(Object entity, Class<T> targetClass) {
        try {
            DtoClassInfo dtoClassInfo = this.getDtoClassInfo(targetClass);
            EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(entity.getClass());

            T object = targetClass.newInstance();
            BeanUtils.copyProperties(entity, object);
            for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                if(!dtoField.getHasEntityClass()) {
                    continue;
                }
                TableFieldInfo entityTableFieldInfo = dtoClassInfo.getEntityClassInfo().getFieldByClass(dtoField.getRelatedEntityClass());
                Object child = FieldUtils.getValue(entity , entityTableFieldInfo.getField());
                Object subObject = convertFromEntity(child, (Class)dtoField.getTableFieldInfo().getActualType());
                FieldUtils.setField(object, dtoField.getTableFieldInfo().getField(), subObject);
            }
            return object;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends Collection> T convertFromEntityList(T entityList, Class<?> targetClass) {
        try {
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
                if(!dtoField.getHasEntityClass()) {
                    continue;
                }
                TableFieldInfo entityTableFieldInfo = dtoClassInfo.getEntityClassInfo().getFieldByClass(dtoField.getRelatedEntityClass());
                Iterator itFrom = entityList.iterator();
                Iterator itTo = objectList.iterator();
                while(itFrom.hasNext()) {
                    Object entity = itFrom.next();
                    if(entity == null) continue;
                    Object child = FieldUtils.getValue(entity, entityTableFieldInfo.getField());
                    Object object = convertFromEntity(child, (Class)dtoField.getTableFieldInfo().getActualType());
                    FieldUtils.setField(itTo.next(), dtoField.getTableFieldInfo().getField(), object);
                }
            }
            return objectList;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
