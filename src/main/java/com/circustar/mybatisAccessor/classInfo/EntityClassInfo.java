package com.circustar.mybatisAccessor.classInfo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class EntityClassInfo {
    private Class<?> entityClass;
    private List<EntityFieldInfo> fieldList;
    private Map<String, EntityFieldInfo> fieldMap;
    private TableInfo tableInfo;

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
}
