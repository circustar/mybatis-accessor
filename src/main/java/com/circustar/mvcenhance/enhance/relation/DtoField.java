package com.circustar.mvcenhance.enhance.relation;

import com.circustar.mvcenhance.common.query.EntityFilter;
import com.circustar.mvcenhance.common.query.QueryField;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DtoField {
    private String fieldName;
    private Class targetClass;
    private Field field;
    private List<QueryField> queryFields;
    private List<EntityFilter> entityFilters;
    private boolean isCollection;
    private Type actualType;
    private Type ownerType;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public List<QueryField> getQueryFields() {
        return queryFields;
    }

    public void setQueryFields(List<QueryField> queryFields) {
        this.queryFields = queryFields;
    }

    public List<EntityFilter> getEntityFilters() {
        return entityFilters;
    }

    public void setEntityFilters(List<EntityFilter> entityFilters) {
        this.entityFilters = entityFilters;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public void setCollection(boolean collection) {
        isCollection = collection;
    }

    public Type getActualType() {
        return actualType;
    }

    public void setActualType(Type actualType) {
        this.actualType = actualType;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(Type ownerType) {
        this.ownerType = ownerType;
    }

    public Object getValue(Object target) {
        field.setAccessible(true);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void setValue(Object target, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(target, value);
    }

    public static List<DtoField> parseDtoFields(Class targetClass) {
        List<DtoField> dtoFields = new ArrayList<>();
        for(Field field : targetClass.getDeclaredFields()) {
            DtoField dtoField = new DtoField();
            dtoField.setField(field);
            dtoField.setFieldName(field.getName());
            dtoField.setTargetClass(targetClass);

            if(Collection.class.isAssignableFrom(field.getType())) {
                dtoField.setCollection(true);
                Type dtoType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                dtoField.setActualType(dtoType);
                dtoField.setOwnerType(((ParameterizedType) field.getGenericType()).getRawType());
            } else {
                dtoField.setCollection(false);
                dtoField.setOwnerType(field.getType());
                dtoField.setActualType(field.getType());
            }
            QueryField[] queryFields = field.getAnnotationsByType(QueryField.class);
            dtoField.setQueryFields(Arrays.asList(queryFields));
            EntityFilter[] entityFilters = field.getAnnotationsByType(EntityFilter.class);
            dtoField.setEntityFilters(Arrays.asList(entityFilters));

            dtoFields.add(dtoField);
        }
        return dtoFields;
    }

}
