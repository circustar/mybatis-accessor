package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.circustar.mvcenhance.utils.TableInfoUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

public class EntityFieldInfo {
    private Field field;

    private TableField tableField;
    private String columnName;
    private boolean isKeyColumn;
    private EntityClassInfo entityClassInfo;

    public Boolean getPrimitive() {
        return isPrimitive;
    }

    public boolean isKeyColumn() {
        return isKeyColumn;
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

    public EntityClassInfo getEntityClassInfo() {
        return entityClassInfo;
    }

    public void setEntityClassInfo(EntityClassInfo entityClassInfo) {
        this.entityClassInfo = entityClassInfo;
    }

    private Boolean isCollection = false;
    private Type actualType = null;
    private Type ownType = null;

    public static EntityFieldInfo parseField(Class c, Field field, EntityClassInfo entityClassInfo) {
        EntityFieldInfo fieldInfo = new EntityFieldInfo();
        fieldInfo.setEntityClassInfo(entityClassInfo);
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
        if(fieldInfo.tableField != null && !StringUtils.isEmpty(fieldInfo.tableField.value())) {
            fieldInfo.columnName = fieldInfo.tableField.value();
        } else {
            fieldInfo.columnName = TableInfoUtils.getDBObjectName(fieldInfo.getField().getName());
        }
        TableId tableId = field.getAnnotation(TableId.class);
        if(tableId != null) {
            fieldInfo.isKeyColumn = true;
            if(!StringUtils.isEmpty(tableId.value())) {
                fieldInfo.columnName = tableId.value();
            }
        }
        return fieldInfo;
    }

    public static EntityFieldInfo parseFieldByName(Class c, String property_name, EntityClassInfo entityClassInfo) {
        try {
            Field field = c.getDeclaredField(property_name.substring(0,1).toLowerCase() + property_name.substring(1));
            if(field== null) {
                return null;
            }
            return parseField(c, field, entityClassInfo);
        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    public static EntityFieldInfo parseFieldByClass(Class c, Class subClass, Boolean findInGenericType, EntityClassInfo entityClassInfo) {
        try {
            if(!findInGenericType) {
                return Arrays.stream(c.getDeclaredFields()).filter(x -> x.getType().getClass() == subClass)
                        .findFirst().map(x -> EntityFieldInfo.parseField(c, x, entityClassInfo)).orElse(null);
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
                        .findFirst().map(x -> EntityFieldInfo.parseField(c, x, entityClassInfo)).orElse(null);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public TableField getTableField() {
        return tableField;
    }

    public String getColumnName() {
        return columnName;
    }
}
