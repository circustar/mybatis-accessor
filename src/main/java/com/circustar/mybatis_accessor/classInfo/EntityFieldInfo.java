package com.circustar.mybatis_accessor.classInfo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.entity.IdReference;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class EntityFieldInfo {
    private Field field;

    private TableField tableField;
    private String columnName;
    private boolean isKeyColumn;
    private EntityClassInfo entityClassInfo;
    private PropertyDescriptor propertyDescriptor;
    private IdReference idReference;
    private boolean isLogicDeleteField;

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
    private Type actualType;
    private Type ownType;

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public IdReference getIdReference() {
        return idReference;
    }

    public void setIdReference(IdReference idReference) {
        this.idReference = idReference;
    }

    public static EntityFieldInfo parseField(Class c, PropertyDescriptor propertyDescriptor, EntityClassInfo entityClassInfo) {
        EntityFieldInfo fieldInfo = new EntityFieldInfo();
        fieldInfo.setEntityClassInfo(entityClassInfo);
        fieldInfo.setPropertyDescriptor(propertyDescriptor);
        Field field = FieldUtils.getField(c ,propertyDescriptor.getName());
        assert(field != null);
        fieldInfo.setField(field);
        IdReference idReference = fieldInfo.getField().getAnnotation(IdReference.class);
        fieldInfo.setIdReference(idReference);
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
        TableLogic tableLogic = fieldInfo.getField().getAnnotation(TableLogic.class);
        fieldInfo.setLogicDeleteField(tableLogic != null);

        return fieldInfo;
    }

    public TableField getTableField() {
        return tableField;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isLogicDeleteField() {
        return isLogicDeleteField;
    }

    public void setLogicDeleteField(boolean logicDeleteField) {
        isLogicDeleteField = logicDeleteField;
    }
}
