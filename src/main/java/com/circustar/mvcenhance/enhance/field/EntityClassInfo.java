package com.circustar.mvcenhance.enhance.field;

import java.util.*;
import java.util.stream.Collectors;

public class EntityClassInfo {
    private Class<?> clazz;
    private List<FieldTypeInfo> fieldList;
    private Map<Class<?>, FieldTypeInfo> entityFieldInfoMap;

    public EntityClassInfo(Class<?> clazz) {
        this.clazz = clazz;
        this.fieldList = Arrays.stream(clazz.getDeclaredFields()).map(x -> {
            FieldTypeInfo fieldTypeInfo = FieldTypeInfo.parseField(this.clazz, x);
            return fieldTypeInfo;
        }).collect(Collectors.toList());
        this.entityFieldInfoMap = new HashMap<>();
        this.fieldList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(p -> p.getActualType().getTypeName())))
                , ArrayList::new)).stream().forEach(x -> {
            this.entityFieldInfoMap.put((Class)x.getActualType(), x);
        });
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<FieldTypeInfo> getFieldList() {
        return fieldList;
    }

    public FieldTypeInfo getFieldByClass(Class<?> clazz) {
        if(entityFieldInfoMap.containsKey(clazz)) {
            return entityFieldInfoMap.get(clazz);
        }
        return null;
    }

    public boolean containsSubEntity() {
        return entityFieldInfoMap.keySet().size() > 0;
    }
}
