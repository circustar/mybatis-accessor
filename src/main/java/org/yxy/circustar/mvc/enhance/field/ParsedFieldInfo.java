package org.yxy.circustar.mvc.enhance.field;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

public class ParsedFieldInfo {
    private Field field;

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

    public static ParsedFieldInfo parseField(Class c, Field field) {
        ParsedFieldInfo fieldInfo = new ParsedFieldInfo();
        fieldInfo.setField(field);
        if(Collection.class.isAssignableFrom(field.getType())
                && field.getGenericType() instanceof ParameterizedType) {
            fieldInfo.setIsCollection(true);
            fieldInfo.setActualType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            fieldInfo.setOwnType(((ParameterizedType) field.getGenericType()).getRawType());
        } else {
            fieldInfo.setIsCollection(false);
            fieldInfo.setActualType(field.getType());
        }
        return fieldInfo;
    }

    public static ParsedFieldInfo parseFieldByName(Class c, String property_name) {
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

    public static ParsedFieldInfo parseFieldByClass(Class c, Class subClass, Boolean findInGenericType) {
        try {
            if(!findInGenericType) {
                return Arrays.stream(c.getDeclaredFields()).filter(x -> x.getType().getClass() == subClass)
                        .findFirst().map(x -> ParsedFieldInfo.parseField(c, x)).orElse(null);
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
                        .findFirst().map(x -> ParsedFieldInfo.parseField(c, x)).orElse(null);
            }
        } catch (Exception e) {
        }
        return null;
    }
}
