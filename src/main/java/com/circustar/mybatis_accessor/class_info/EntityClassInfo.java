package com.circustar.mybatis_accessor.class_info;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.entity.UpdateOrder;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class EntityClassInfo {
    private final Class<?> entityClass;
    private final List<EntityFieldInfo> fieldList;
    private final Map<String, EntityFieldInfo> fieldMap;
    private TableInfo tableInfo;
    private EntityFieldInfo keyField;
    private final EntityFieldInfo idReferenceFieldInfo;
    private int updateOrder = Integer.MAX_VALUE;

    public EntityClassInfo(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
        this.fieldList = FieldUtils.getPropertyDescriptors(entityClass).stream().map(x -> {
            return EntityFieldInfo.parseField(this.entityClass, x, this);
        }).collect(Collectors.toList());
        this.fieldMap = this.fieldList.stream().collect(Collectors.toMap(x -> x.getField().getName(), x -> x));

        if(this.tableInfo == null) {
            TableName tableName = this.entityClass.getAnnotation(TableName.class);
            if(tableName != null && !StringUtils.isEmpty(tableName.value())) {
                this.tableInfo = TableInfoHelper.getTableInfo(tableName.value());
            }
        }
        if(this.tableInfo != null && !StringUtils.isEmpty(this.tableInfo.getKeyProperty())) {
            this.keyField = this.fieldMap.get(this.tableInfo.getKeyProperty());
        }

        this.idReferenceFieldInfo = this.fieldList.stream().filter(x -> x.getIdReference() != null).findAny().orElse(null);

        UpdateOrder updateOrderAnno = this.entityClass.getAnnotation(UpdateOrder.class);
        if(updateOrderAnno != null) {
            this.updateOrder = updateOrderAnno.value();
        }
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public List<EntityFieldInfo> getFieldList() {
        return fieldList;
    }

    public EntityFieldInfo getFieldByName(String fieldName) {
        return fieldMap.get(fieldName);
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public EntityFieldInfo getKeyField() {
        return keyField;
    }

    public EntityFieldInfo getIdReferenceFieldInfo() {
        return idReferenceFieldInfo;
    }

    public int getUpdateOrder() {
        return updateOrder;
    }
}
