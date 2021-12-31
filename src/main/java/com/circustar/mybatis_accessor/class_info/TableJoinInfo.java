package com.circustar.mybatis_accessor.class_info;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.dto.QueryJoin;
import com.circustar.mybatis_accessor.model.QueryJoinModel;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class TableJoinInfo {
    private String fieldName;
    private Class targetClass;
    private Field field;
    private QueryJoinModel queryJoin;
    private boolean collection;
    private Class actualClass;
    private Class ownerClass;
    private TableInfo tableInfo;
    private Integer position;

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

    public QueryJoinModel getQueryJoin() {
        return queryJoin;
    }

    public void setQueryJoin(QueryJoinModel queryJoin) {
        this.queryJoin = queryJoin;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public Class getActualClass() {
        return actualClass;
    }

    public void setActualClass(Class actualClass) {
        this.actualClass = actualClass;
    }

    public Class getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(Class ownerClass) {
        this.ownerClass = ownerClass;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public static TableJoinInfo parseDtoFieldJoinInfo(DtoClassInfoHelper dtoClassInfoHelper, Class dtoClass, DtoField dtoField) {
        if(dtoField.getField() == null) {
            return null;
        }
        Field field = dtoField.getField();
        QueryJoin queryJoin = field.getAnnotation(QueryJoin.class);
        if(queryJoin == null) {
            return null;
        }

        TableJoinInfo tableJoinInfo = new TableJoinInfo();
        tableJoinInfo.setField(field);
        tableJoinInfo.setFieldName(field.getName());
        tableJoinInfo.setTargetClass(dtoClass);

        Type dtoFieldType = field.getType();
        if(Collection.class.isAssignableFrom((Class<?>) dtoFieldType)) {
            dtoFieldType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        }
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo((Class<?>) dtoFieldType);
        if(dtoClassInfo == null) {
            return null;
        }
        if(Collection.class.isAssignableFrom(field.getType())) {
            tableJoinInfo.setCollection(true);
            tableJoinInfo.setOwnerClass((Class) ((ParameterizedType) field.getGenericType()).getRawType());
            tableJoinInfo.setActualClass(dtoClassInfo.getEntityClassInfo().getEntityClass());
        } else {
            tableJoinInfo.setCollection(false);
            tableJoinInfo.setActualClass(dtoClassInfo.getEntityClassInfo().getEntityClass());
            tableJoinInfo.setActualClass(dtoClassInfo.getEntityClassInfo().getEntityClass());
        }

        tableJoinInfo.setTableInfo(TableInfoHelper.getTableInfo(tableJoinInfo.getActualClass()));

        QueryJoinModel queryJoinModel = new QueryJoinModel(queryJoin);
        if(StringUtils.isEmpty(queryJoinModel.getTableAlias())) {
            queryJoinModel.setTableAlias(tableJoinInfo.getTableInfo().getTableName());
        }

        tableJoinInfo.setQueryJoin(queryJoinModel);

        return tableJoinInfo;
    }

    public static List<TableJoinInfo> parseEntityTableJoinInfo(Configuration configuration, Class entityClass) {
        List<TableJoinInfo> tableJoinInfos = new ArrayList<>();
        List<PropertyDescriptor> propertyDescriptors = FieldUtils.getPropertyDescriptors(entityClass);
        for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Field field = FieldUtils.getField(entityClass, propertyDescriptor.getName());
            TableField[] tableField = field.getAnnotationsByType(TableField.class);
            if(tableField == null || tableField.length == 0 || tableField[0].exist()) {
                continue;
            }
            Class clazz = field.getType();
            if(clazz == null || TableInfoUtils.isMybatisSupportType(clazz)) {
                continue;
            }
            TypeHandler<? extends TableField[]> typeHandler = configuration.getTypeHandlerRegistry().getTypeHandler(clazz);
            if(typeHandler != null) {
                continue;
            }
            TableJoinInfo tableJoinInfo = new TableJoinInfo();
            tableJoinInfo.setField(field);
            tableJoinInfo.setFieldName(field.getName());
            tableJoinInfo.setTargetClass(entityClass);

            if(Collection.class.isAssignableFrom(field.getType())) {
                tableJoinInfo.setCollection(true);
                Class dtoClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                tableJoinInfo.setActualClass(dtoClass);
                tableJoinInfo.setOwnerClass((Class) ((ParameterizedType) field.getGenericType()).getRawType());
            } else {
                tableJoinInfo.setCollection(false);
                tableJoinInfo.setOwnerClass(field.getType());
                tableJoinInfo.setActualClass(field.getType());
            }

            tableJoinInfos.add(tableJoinInfo);
        }
        setPosition(tableJoinInfos);

        return tableJoinInfos;
    }

    public static void setPosition(List<TableJoinInfo> tableJoinInfos) {
        Map<Class, List<TableJoinInfo>> infoMap = tableJoinInfos.stream().collect(Collectors.groupingBy(x -> x.getActualClass()));
        for(Map.Entry<Class, List<TableJoinInfo>> info : infoMap.entrySet()) {
            int position = 0;
            List<TableJoinInfo> var0 = info.getValue();
            for(TableJoinInfo tableJoinInfo : var0) {
                tableJoinInfo.setPosition(position++);
            }
        }
    }

}
