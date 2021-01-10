package com.circustar.mvcenhance.enhance.field;

import com.baomidou.mybatisplus.annotation.TableField;
import com.circustar.mvcenhance.common.query.QueryField;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

public class FieldTypeInfo {
    private Field field;

    private TableField tableField;

    private QueryField[] queryFields;

    public Boolean getPrimitive() {
        return isPrimitive;
    }

    public Boolean getCollection() {
        return isCollection;
    }

    private Boolean isPrimitive;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Boolean getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(Boolean collection) {
        isCollection = collection;
    }

    public Type getActualType() {
        return actualType;
    }

    public void setActualType(Type actualType) {
        this.actualType = actualType;
    }

    public Type getOwnType() {
        return ownType;
    }

    public void setOwnType(Type ownType) {
        this.ownType = ownType;
    }

    private Boolean isCollection = false;
    private Type actualType = null;
    private Type ownType = null;

    public static FieldTypeInfo parseField(Class c, Field field) {
        FieldTypeInfo fieldInfo = new FieldTypeInfo();
        fieldInfo.setField(field);
        if(Collection.class.isAssignableFrom(field.getType())
                && field.getGenericType() instanceof ParameterizedType) {
            fieldInfo.setIsCollection(true);
            fieldInfo.setActualType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            fieldInfo.setOwnType(((ParameterizedType) field.getGenericType()).getRawType());
            fieldInfo.isPrimitive = false;
        } else {
            fieldInfo.setIsCollection(false);
            fieldInfo.setActualType(field.getType());
            fieldInfo.isPrimitive = field.getType().isPrimitive();
        }
        fieldInfo.tableField = field.getAnnotation(TableField.class);
        fieldInfo.queryFields = field.getAnnotationsByType(QueryField.class);
        return fieldInfo;
    }

    public static FieldTypeInfo parseFieldByName(Class c, String property_name) {
        try {
            Field field = c.getDeclaredField(property_name.substring(0,1).toLowerCase() + property_name.substring(1));
            if(field== null) {
                return null;
            }
            return parseField(c, field);
        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    public static FieldTypeInfo parseFieldByClass(Class c, Class subClass, Boolean findInGenericType) {
        try {
            if(!findInGenericType) {
                return Arrays.stream(c.getDeclaredFields()).filter(x -> x.getType().getClass() == subClass)
                        .findFirst().map(x -> FieldTypeInfo.parseField(c, x)).orElse(null);
            } else {
                return Arrays.stream(c.getDeclaredFields())
                        .filter(x -> {
                            if(x.getType().getClass() == subClass) {
                                return true;
                            }

                            if(!(x.getGenericType() instanceof ParameterizedType)) {
                                return false;
                            }
                            Type dtoType = ((ParameterizedType) x.getGenericType()).getActualTypeArguments()[0];
                            return (dtoType == subClass);
                        })
                        .findFirst().map(x -> FieldTypeInfo.parseField(c, x)).orElse(null);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public TableField getTableField() {
        return tableField;
    }

    public QueryField[] getQueryFields() {
        return queryFields;
    }
}
