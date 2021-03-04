package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class EntityClassInfo {
    private Class<?> clazz;
    private List<TableFieldInfo> fieldList;
    private Map<Class<?>, TableFieldInfo> entityFieldInfoMap;
    private Map<String, TableFieldInfo> fieldMap;
    private TableInfo tableInfo;

    public EntityClassInfo(Class<?> clazz) {
        this.clazz = clazz;
        this.fieldList = Arrays.stream(clazz.getDeclaredFields()).map(x -> {
            TableFieldInfo tableFieldInfo = TableFieldInfo.parseField(this.clazz, x);
            return tableFieldInfo;
        }).collect(Collectors.toList());
        this.entityFieldInfoMap = new HashMap<>();
        this.fieldList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(p -> p.getActualType().getTypeName())))
                , ArrayList::new)).stream().forEach(x -> {
            this.entityFieldInfoMap.put((Class)x.getActualType(), x);
        });
        this.fieldMap = this.fieldList.stream().collect(Collectors.toMap(x -> x.getField().getName(), x -> x));
        this.tableInfo = TableInfoHelper.getTableInfo(this.clazz);
        if(this.tableInfo == null) {
            TableName tableName = this.clazz.getAnnotation(TableName.class);
            if(tableName != null && !StringUtils.isEmpty(tableName.value())) {
                this.tableInfo = TableInfoHelper.getTableInfo(tableName.value());
            }
        }
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<TableFieldInfo> getFieldList() {
        return fieldList;
    }

    public TableFieldInfo getFieldByClass(Class<?> clazz) {
        if(entityFieldInfoMap.containsKey(clazz)) {
            return entityFieldInfoMap.get(clazz);
        }
        return null;
    }

    public TableFieldInfo getFieldByName(String fieldName) {
        if(fieldMap.containsKey(fieldName)) {
            return fieldMap.get(fieldName);
        }
        return null;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }
}
