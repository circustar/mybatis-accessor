package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.circustar.mvcenhance.annotation.JoinTable;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TableJoinInfo {
    private String fieldName;
    private Class targetClass;
    private Field field;
    private JoinTable joinTable;
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

    public JoinTable getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(JoinTable joinTable) {
        this.joinTable = joinTable;
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

    public static List<TableJoinInfo> parseDtoTableJoinInfo(Class targetClass) {
        List<TableJoinInfo> tableJoinInfos = new ArrayList<>();
        for(Field field : targetClass.getDeclaredFields()) {
            JoinTable[] joinColumns = field.getAnnotationsByType(JoinTable.class);
            if(joinColumns == null || joinColumns.length == 0) {
                continue;
            }
            JoinTable joinTable = joinColumns[0];
            TableJoinInfo tableJoinInfo = new TableJoinInfo();
            tableJoinInfo.setField(field);
            tableJoinInfo.setFieldName(field.getName());
            tableJoinInfo.setTargetClass(targetClass);

            if(Collection.class.isAssignableFrom(field.getType())) {
                tableJoinInfo.setCollection(true);
                Type dtoType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                tableJoinInfo.setActualType(dtoType);
                tableJoinInfo.setOwnerType(((ParameterizedType) field.getGenericType()).getRawType());
            } else {
                tableJoinInfo.setCollection(false);
                tableJoinInfo.setOwnerType(field.getType());
                tableJoinInfo.setActualType(field.getType());
            }

            tableJoinInfo.setJoinTable(joinTable);

            tableJoinInfos.add(tableJoinInfo);
        }
        return tableJoinInfos;
    }

    public static List<TableJoinInfo> parseEntityTableJoinInfo(Configuration configuration, Class targetClass) {
        List<TableJoinInfo> tableJoinInfos = new ArrayList<>();
        for(Field field : targetClass.getDeclaredFields()) {
            TableField[] tableField = field.getAnnotationsByType(TableField.class);
            if(tableField == null || tableField.length == 0 || tableField[0].exist()) {
                continue;
            }
            TypeHandler<? extends TableField[]> typeHandler = configuration.getTypeHandlerRegistry().getTypeHandler((Class)field.getType());
            if(typeHandler != null) {
                continue;
            }
            TableJoinInfo tableJoinInfo = new TableJoinInfo();
            tableJoinInfo.setField(field);
            tableJoinInfo.setFieldName(field.getName());
            tableJoinInfo.setTargetClass(targetClass);

            if(Collection.class.isAssignableFrom(field.getType())) {
                tableJoinInfo.setCollection(true);
                Type dtoType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                tableJoinInfo.setActualType(dtoType);
                tableJoinInfo.setOwnerType(((ParameterizedType) field.getGenericType()).getRawType());
            } else {
                tableJoinInfo.setCollection(false);
                tableJoinInfo.setOwnerType(field.getType());
                tableJoinInfo.setActualType(field.getType());
            }

            tableJoinInfos.add(tableJoinInfo);
        }
        return tableJoinInfos;
    }

}
