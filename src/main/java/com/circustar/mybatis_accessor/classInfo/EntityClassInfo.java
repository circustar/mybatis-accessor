package com.circustar.mybatis_accessor.classInfo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.circustar.mybatis_accessor.common.MessageProperties;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class EntityClassInfo {
    private Class<?> entityClass;
    private List<EntityFieldInfo> fieldList;
    private Map<String, EntityFieldInfo> fieldMap;
    private TableInfo tableInfo;
    private EntityFieldInfo keyField;
    private EntityFieldInfo idReferenceFieldInfo;

    public EntityClassInfo(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
        this.fieldList = Arrays.stream(entityClass.getDeclaredFields()).map(x -> {
            EntityFieldInfo entityFieldInfo = EntityFieldInfo.parseField(this.entityClass, x, this);
            return entityFieldInfo;
        }).collect(Collectors.toList());
        this.fieldMap = this.fieldList.stream().collect(Collectors.toMap(x -> x.getField().getName(), x -> x));

        if(this.tableInfo == null) {
            TableName tableName = this.entityClass.getAnnotation(TableName.class);
            if(tableName != null && !StringUtils.isEmpty(tableName.value())) {
                this.tableInfo = TableInfoHelper.getTableInfo(tableName.value());
            }
        }
        if(!StringUtils.isEmpty(this.tableInfo.getKeyProperty())) {
            this.keyField = this.fieldMap.get(this.tableInfo.getKeyProperty());
        }

        this.idReferenceFieldInfo = this.fieldList.stream().filter(x -> x.getIdReference() != null).findAny().orElse(null);
        if(this.idReferenceFieldInfo != null && this.keyField == null) {
            throw new RuntimeException(String.format(MessageProperties.ID_REFERENCE_NOT_FOUND
                    , this.entityClass.getSimpleName()
                    ,this.idReferenceFieldInfo.getField().getName()));
        }
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public List<EntityFieldInfo> getFieldList() {
        return fieldList;
    }

    public EntityFieldInfo getFieldByName(String fieldName) {
        if(fieldMap.containsKey(fieldName)) {
            return fieldMap.get(fieldName);
        }
        return null;
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
}
