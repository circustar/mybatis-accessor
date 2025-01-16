package com.circustar.mybatis_accessor.class_info;

import com.circustar.common_utils.reflection.ClassUtils;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.service.IUpdateService;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DtoClassInfoHelper {
    private final static Map<Class<?>, DtoClassInfo> DTO_CLASS_INFO_MAP = new ConcurrentHashMap<>();

    private final IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private final EntityClassInfoHelper entityClassInfoHelper;

    private ISelectService selectService;

    private IUpdateService updateService;

    private final ApplicationContext applicationContext;

    public static synchronized void resetMap() {
        DTO_CLASS_INFO_MAP.values().stream().forEach(x -> x.getEntityDtoServiceRelation().setDtoClassInfo(null));
        DTO_CLASS_INFO_MAP.clear();
    }

    public DtoClassInfoHelper(ApplicationContext applicationContext
            , IEntityDtoServiceRelationMap relationMap, EntityClassInfoHelper entityClassInfoHelper) {
        this.applicationContext = applicationContext;
        this.entityDtoServiceRelationMap = relationMap;
        this.entityClassInfoHelper = entityClassInfoHelper;
    }
    public DtoClassInfo getDtoClassInfo(EntityDtoServiceRelation relation) {
        if(relation.getDtoClassInfo() != null) {
            return relation.getDtoClassInfo();
        }
        return getDtoClassInfo(relation.getDtoClass());
    }
    public DtoClassInfo getDtoClassInfo(Class<?> clazz) {
        if(DTO_CLASS_INFO_MAP.containsKey(clazz)) {
            return DTO_CLASS_INFO_MAP.get(clazz);
        }
        EntityClassInfo entityClassInfo = entityClassInfoHelper.getEntityClassInfo(entityDtoServiceRelationMap.getByDtoClass(clazz).getEntityClass());
        DtoClassInfo dtoClassInfo = new DtoClassInfo(entityDtoServiceRelationMap, this, clazz, entityClassInfo);
        DTO_CLASS_INFO_MAP.put(clazz, dtoClassInfo);
        dtoClassInfo.initJoinTableInfo(this);
        return dtoClassInfo;
    }

    public void setSelectService(ISelectService selectService) {
        this.selectService = selectService;
    }

    public void setUpdateService(IUpdateService updateService) {
        this.updateService = updateService;
    }

    public ISelectService getSelectService() {
        return selectService;
    }

    public IUpdateService getUpdateService() {
        return updateService;
    }

    public Object convertToEntity(Object object, DtoClassInfo dtoClassInfo, boolean isWithSubFields) {
        if(object == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(object.getClass())) {
            return convertToEntityList((Collection)object, dtoClassInfo, isWithSubFields);
        } else {
            return convertToSingleEntity(object, dtoClassInfo, isWithSubFields);
        }
    }

    public Object convertToSingleEntity(Object object, DtoClassInfo dtoClassInfo, boolean isWithSubFields) {
        Object entity = dtoClassInfo.convertToEntity(object);
        if(isWithSubFields) {
            EntityClassInfo entityClassInfo = dtoClassInfo.getEntityClassInfo();
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                EntityFieldInfo entityEntityFieldInfo = entityClassInfo.getFieldByName(dtoField.getField().getName());
                if (entityEntityFieldInfo == null) {
                    continue;
                }
                Object subDto = FieldUtils.getFieldValue(object, dtoField.getPropertyDescriptor().getReadMethod());
                Object child = convertToEntity(subDto, dtoField.getFieldDtoClassInfo(), isWithSubFields);
                FieldUtils.setFieldValue(entity, entityEntityFieldInfo.getPropertyDescriptor().getWriteMethod(), child);
            }
        }
        return entity;
    }

    public <T extends Collection> T convertToEntityList(T objects, DtoClassInfo dtoClassInfo, boolean isWithSubFields) {
        Class collectionClass = objects.getClass();
        Class<? extends Collection> implementClass = CollectionType.getSupportCollectionType(collectionClass);
        Collection childList = ClassUtils.createInstance(implementClass);
        Iterator iterator = objects.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object == null) {continue;}
            Object child = dtoClassInfo.convertToEntity(object);
            childList.add(child);
        }
        if(isWithSubFields) {
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                EntityFieldInfo entityEntityFieldInfo = dtoField.getEntityFieldInfo();
                if (entityEntityFieldInfo == null || !entityEntityFieldInfo.isCollection()) {
                    continue;
                }
                Iterator itFrom = objects.iterator();
                Iterator itTo = childList.iterator();
                while (itFrom.hasNext()) {
                    Object object = FieldUtils.getFieldValue(itFrom.next(), dtoField.getPropertyDescriptor().getReadMethod());
                    Object child = convertToEntity(object, dtoField.getFieldDtoClassInfo(), isWithSubFields);
                    FieldUtils.setFieldValue(itTo.next(), entityEntityFieldInfo.getPropertyDescriptor().getWriteMethod(), child);
                }
            }
        }
        return (T) childList;
    }


    public Object convertFromEntity(Object entity, DtoClassInfo dtoClassInfo) {
        if(entity == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(entity.getClass())) {
            return convertFromEntityList((Collection)entity, dtoClassInfo);
        } else {
            return convertFromSingleEntity(entity, dtoClassInfo);
        }
    }


    public Object convertFromSingleEntity(Object entity, DtoClassInfo dtoClassInfo)  {
        Object object = dtoClassInfo.convertFromEntity(entity);
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
    }

    public <T extends Collection, R extends Collection> R convertFromEntityList(T entityList, DtoClassInfo dtoClassInfo) {
        Class collectionClass = entityList.getClass();
        Class<T> implementClass = (Class<T>) CollectionType.getSupportCollectionType(collectionClass);
        R objectList = (R) ClassUtils.createInstance(implementClass);
        Iterator iterator = entityList.iterator();
        while (iterator.hasNext()) {
            Object entity = iterator.next();
            if (entity == null) {
                objectList.add(null);
                continue;
            }
            Object object = dtoClassInfo.convertFromEntity(entity);
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
                if (entity == null) {continue;}
                Object child = FieldUtils.getFieldValue(entity, entityEntityFieldInfo.getPropertyDescriptor().getReadMethod());
                if (child == null) {
                    continue;
                }
                Object object = convertFromEntity(child, dtoField.getFieldDtoClassInfo());
                FieldUtils.setFieldValue(itTo.next(), dtoField.getPropertyDescriptor().getWriteMethod(), object);
            }
        }
        return objectList;
    }

    public IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        return entityDtoServiceRelationMap;
    }

    public EntityClassInfoHelper getEntityClassInfoHelper() {
        return entityClassInfoHelper;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }
}
