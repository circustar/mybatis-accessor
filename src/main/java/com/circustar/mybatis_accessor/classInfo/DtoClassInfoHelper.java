package com.circustar.mybatis_accessor.classInfo;

import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;

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
        DtoClassInfo dtoClassInfo = new DtoClassInfo(entityDtoServiceRelationMap, this, clazz, entityClassInfo);
        dtoClassInfo = tryPut(clazz, dtoClassInfo);
        dtoClassInfo.initJoinTableInfo(this);
        return dtoClassInfo;
    }

    private DtoClassInfo tryPut(Class<?> clazz, DtoClassInfo dtoClassInfo) {
        try {
            dtoClassInfoMap.put(clazz, dtoClassInfo);
        } catch (Exception ex) {
        }
        return dtoClassInfoMap.get(clazz);

    }

    public Object convertToEntity(Object object, DtoClassInfo dtoClassInfo) {
        if(object == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(object.getClass())) {
            return convertToEntityList((Collection)object, dtoClassInfo);
        } else {
            return convertToSingleObject(object, dtoClassInfo);
        }
    }

    public Object convertToSingleObject(Object object, DtoClassInfo dtoClassInfo) {
        try {
            Class targetClass = dtoClassInfo.getEntityDtoServiceRelation().getEntityClass();
            EntityClassInfo entityClassInfo = dtoClassInfo.getEntityClassInfo();

            Object entity = targetClass.newInstance();
            BeanUtils.copyProperties(object, entity);
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                EntityFieldInfo entityEntityFieldInfo = entityClassInfo.getFieldByName(dtoField.getField().getName());
                if (entityEntityFieldInfo == null) {
                    continue;
                }
                Object subDto = FieldUtils.getFieldValue(object, dtoField.getPropertyDescriptor().getReadMethod());
                Object child = convertToEntity(subDto, dtoField.getFieldDtoClassInfo());
                FieldUtils.setFieldValue(entity, entityEntityFieldInfo.getPropertyDescriptor().getWriteMethod(), child);
            }
            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends Collection> T convertToEntityList(T objects, DtoClassInfo dtoClassInfo) {
        Class collectionClass = objects.getClass();
        Class<? extends Collection> implementClass = CollectionType.getSupportCollectionType(collectionClass);
        if(implementClass == null) {
            throw new RuntimeException("Collection type not Support!");
        }
        try {
            EntityClassInfo entityClassInfo = dtoClassInfo.getEntityClassInfo();
            Collection childList = implementClass.newInstance();
            Iterator it = objects.iterator();
            while (it.hasNext()) {
                Object object = it.next();
                if (object == null) continue;
                Object child = entityClassInfo.getEntityClass().newInstance();
                BeanUtils.copyProperties(object, child);
                childList.add(child);
            }
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                EntityFieldInfo entityEntityFieldInfo = dtoField.getEntityFieldInfo();
                if (entityEntityFieldInfo == null || !entityEntityFieldInfo.getIsCollection()) {
                    continue;
                }
                Iterator itFrom = objects.iterator();
                Iterator itTo = childList.iterator();
                while (itFrom.hasNext()) {
                    Object object = FieldUtils.getFieldValue(itFrom.next(), dtoField.getPropertyDescriptor().getReadMethod());
                    Object child = convertToEntity(object, dtoField.getFieldDtoClassInfo());
                    FieldUtils.setFieldValue(itTo.next(), entityEntityFieldInfo.getPropertyDescriptor().getWriteMethod(), child);
                }
            }
            return (T) childList;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public Object convertFromEntity(Object entity, DtoClassInfo dtoClassInfo) {
        if(entity == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(entity.getClass())) {
            return convertFromEntityList((Collection)entity, dtoClassInfo);
        } else {
            return convertFromSingleObject(entity, dtoClassInfo);
        }
    }


    public <T> T convertFromSingleObject(Object entity, DtoClassInfo dtoClassInfo)  {
        try {
            T object = (T) dtoClassInfo.getClazz().newInstance();
            BeanUtils.copyProperties(entity, object);
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                if (dtoField.getEntityFieldInfo() == null) {
                    continue;
                }
                EntityFieldInfo entityEntityFieldInfo = dtoClassInfo.getEntityClassInfo().getFieldByName(dtoField.getField().getName());
                Object child = FieldUtils.getFieldValue(entity, entityEntityFieldInfo.getPropertyDescriptor().getReadMethod());
                Object subObject = convertFromEntity(child, dtoField.getFieldDtoClassInfo());
                FieldUtils.setFieldValue(object, dtoField.getPropertyDescriptor().getWriteMethod(), subObject);
            }
            return object;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends Collection, R extends Collection> R convertFromEntityList(T entityList, DtoClassInfo dtoClassInfo) {
        try {
            Class collectionClass = entityList.getClass();
            Class<T> implementClass = (Class<T>) CollectionType.getSupportCollectionType(collectionClass);
            if (implementClass == null) {
                throw new RuntimeException("Collection type not Support!");
            }
            R objectList = (R) implementClass.newInstance();
            Iterator it = entityList.iterator();
            while (it.hasNext()) {
                Object entity = it.next();
                if (entity == null) {
                    objectList.add(null);
                    continue;
                }
                Object object = dtoClassInfo.getClazz().newInstance();
                BeanUtils.copyProperties(entity, object);
                objectList.add(object);
            }
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                if (dtoField.getEntityFieldInfo() == null) {
                    continue;
                }
                EntityFieldInfo entityEntityFieldInfo = dtoField.getEntityFieldInfo();
                Iterator itFrom = entityList.iterator();
                Iterator itTo = objectList.iterator();
                while (itFrom.hasNext()) {
                    Object entity = itFrom.next();
                    if (entity == null) continue;
                    Object child = FieldUtils.getFieldValue(entity, entityEntityFieldInfo.getPropertyDescriptor().getReadMethod());
                    if (child == null) {
                        continue;
                    }
                    Object object = convertFromEntity(child, dtoField.getFieldDtoClassInfo());
                    FieldUtils.setFieldValue(itTo.next(), dtoField.getPropertyDescriptor().getWriteMethod(), object);
                }
            }
            return objectList;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        return entityDtoServiceRelationMap;
    }

    public EntityClassInfoHelper getEntityClassInfoHelper() {
        return entityClassInfoHelper;
    }

    public ApplicationContext getApplicationContext() {
        return this.entityDtoServiceRelationMap.getApplicationContext();
    }
}
