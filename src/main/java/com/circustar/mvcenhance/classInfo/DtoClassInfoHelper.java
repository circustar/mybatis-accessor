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
        EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(entityDtoServiceRelationMap.getByDtoClass(clazz).getEntity());
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
            Class targetClass = dtoClassInfo.getEntityDtoServiceRelation().getEntity();
            EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(targetClass);

            Object entity = targetClass.newInstance();
            BeanUtils.copyProperties(object, entity);
            for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                FieldTypeInfo entityFieldTypeInfo = entityClassInfo.getFieldByClass(dtoField.getEntityDtoServiceRelation().getEntity());
                if(entityFieldTypeInfo == null) {
                    continue;
                }
                Object subDto = FieldUtils.getValue(object ,dtoField.getFieldTypeInfo().getField());
                Object subEntity = convertToEntity(subDto);
                FieldUtils.setField(entity, entityFieldTypeInfo.getField(), subEntity);
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
            Collection subEntityList =  implementClass.newInstance();
            Iterator it = objects.iterator();
            while(it.hasNext()) {
                Object object = it.next();
                if(object == null) continue;
                if(dtoClass == null) {
                    dtoClass = object.getClass();
                    dtoClassInfo = this.getDtoClassInfo(dtoClass);
                    targetClass = dtoClassInfo.getEntityDtoServiceRelation().getEntity();
                    entityClassInfo = entityClassInfoHelper.getEntityClassInfo(targetClass);
                }
                Object subEntity = entityClassInfo.getClazz().newInstance();
                BeanUtils.copyProperties(object, subEntity);
                subEntityList.add(subEntity);
            }
            if(dtoClass == null) {return (T)subEntityList;}
            for(DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                FieldTypeInfo entityFieldTypeInfo = entityClassInfo.getFieldByClass(dtoField.getEntityDtoServiceRelation().getEntity());
                if(entityFieldTypeInfo == null && !entityFieldTypeInfo.getIsCollection()) {
                    continue;
                }
                Iterator itFrom = objects.iterator();
                Iterator itTo = subEntityList.iterator();
                while(itFrom.hasNext()) {
                    Object object = FieldUtils.getValue(itFrom.next(), dtoField.getFieldTypeInfo().getField());
                    Object subEntity = convertToEntity(object);
                    FieldUtils.setField(itTo.next(), entityFieldTypeInfo.getField(), subEntity);
                }
            }
            return (T) subEntityList;

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
                FieldTypeInfo entityFieldTypeInfo = dtoClassInfo.getEntityClassInfo().getFieldByClass(dtoField.getRelatedEntityClass());
                Object subEntity = FieldUtils.getValue(entity ,entityFieldTypeInfo.getField());
                Object subObject = convertFromEntity(subEntity, (Class)dtoField.getFieldTypeInfo().getActualType());
                FieldUtils.setField(object, dtoField.getFieldTypeInfo().getField(), subObject);
            }
            return object;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends Collection> Collection convertFromEntityList(T entityList, Class<?> targetClass) {
        try {
            Class collectionClass = entityList.getClass();
            Class<? extends Collection> implementClass = CollectionType.getSupportCollectionType(collectionClass);
            if(implementClass == null) {
                throw new Exception("Collection type not Support!");
            }
            DtoClassInfo dtoClassInfo = this.getDtoClassInfo(targetClass);
            Collection objectList =  implementClass.newInstance();
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
                FieldTypeInfo entityFieldTypeInfo = dtoClassInfo.getEntityClassInfo().getFieldByClass(dtoField.getRelatedEntityClass());
                Iterator itFrom = entityList.iterator();
                Iterator itTo = objectList.iterator();
                while(itFrom.hasNext()) {
                    Object entity = itFrom.next();
                    if(entity == null) continue;
                    Object subEntity = FieldUtils.getValue(entity, entityFieldTypeInfo.getField());
                    Object object = convertFromEntity(subEntity, (Class)dtoField.getFieldTypeInfo().getActualType());
                    FieldUtils.setField(itTo.next(), dtoField.getFieldTypeInfo().getField(), object);
                }
            }
            return objectList;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
