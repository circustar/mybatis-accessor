package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.mvcenhance.annotation.DeleteFlag;
import com.circustar.mvcenhance.annotation.JoinTable;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.utils.AnnotationUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DtoClassInfo {
    private Class<?> clazz;
    private IEntityDtoServiceRelationMap relationMap;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private List<DtoField> subDtoFieldList;
    private List<DtoField> normalFieldList;
    private Map<String, DtoField> dtoFieldMap;
    private EntityClassInfo entityClassInfo;
    private String joinTables;
    private String jointColumns;
    private DtoField versionField;
    private Object versionDefaultValue;
    private DtoField keyField;
    private DtoField deleteFlagField;
    public DtoClassInfo(IEntityDtoServiceRelationMap relationMap, Class<?> clazz, EntityClassInfo entityClassInfo) {
        this.clazz = clazz;
        this.relationMap = relationMap;
        this.entityDtoServiceRelation = relationMap.getByDtoClass(this.clazz);
        this.entityClassInfo = entityClassInfo;
        this.subDtoFieldList = new ArrayList<>();
        this.normalFieldList = new ArrayList<>();
        this.dtoFieldMap = new HashMap<>();

        String versionPropertyName = null;
        if(entityClassInfo.getTableInfo().getVersionFieldInfo() != null) {
            entityClassInfo.getTableInfo().getVersionFieldInfo().getProperty();
        }
        String keyProperty = entityClassInfo.getTableInfo().getKeyProperty();
        Arrays.stream(clazz.getDeclaredFields()).forEach(x -> {
            FieldTypeInfo fieldTypeInfo = FieldTypeInfo.parseField(this.clazz, x);
            EntityDtoServiceRelation relation = relationMap.getByDtoClass((Class)fieldTypeInfo.getActualType());
            DtoField dtoField = null;
            if(relation != null) {
                dtoField = new DtoField(x.getName(), fieldTypeInfo, this, relation);
                subDtoFieldList.add(dtoField);
            } else {
                dtoField = new DtoField(x.getName(), fieldTypeInfo, this, null);
                normalFieldList.add(dtoField);
                if(x.getName().equals(versionPropertyName)) {
                    this.versionField = dtoField;
                    this.versionDefaultValue = getDefaultVersionByType(fieldTypeInfo.getField().getType());
                }
            }
            if(x.getName().equals(keyProperty)) {
                this.keyField = dtoField;
            }
            DeleteFlag deleteFlagAnnotation = AnnotationUtils.getFieldAnnotation(fieldTypeInfo.getField(), DeleteFlag.class);
            if(deleteFlagAnnotation != null) {
                this.deleteFlagField = dtoField;
            }
            this.dtoFieldMap.put(x.getName(), dtoField);
        });

        this.subDtoFieldList.forEach(x -> {
            if (x.getHasEntityClass() == null) {
                EntityDtoServiceRelation relation = relationMap.getByDtoClass((Class) x.getFieldTypeInfo().getActualType());
                FieldTypeInfo fieldTypeInfo = this.entityClassInfo.getFieldByClass(relation.getEntityClass());
                if(fieldTypeInfo != null && fieldTypeInfo.getIsCollection() == x.getFieldTypeInfo().getIsCollection()) {
                    x.setHasEntityClass(true);
                    x.setRelatedEntityClass((Class) fieldTypeInfo.getActualType());
                } else {
                    x.setHasEntityClass(false);
                }
            }
        });

//        String masterTableName = TableInfoHelper.getTableInfo(entityClassInfo.getClazz()).getTableName();
        String masterTableName = entityClassInfo.getTableInfo().getTableName();
        List<String> joinTableList = new ArrayList<>();
        List<String> joinColumnList = new ArrayList<>();
        List<TableJoinInfo> tableJoinInfoList = TableJoinInfo.parseDtoTableJoinInfo(clazz);
        tableJoinInfoList.stream().sorted(Comparator.comparingInt(x -> x.getJoinTable().order()))
                .forEach(tableJoinInfo -> {
            Class joinClazz = (Class) tableJoinInfo.getActualType();

            TableInfo joinTableInfo = TableInfoHelper.getTableInfo(relationMap.getByDtoClass(joinClazz).getEntityClass());
            String strAlias = tableJoinInfo.getJoinTable().alias();
            joinTableList.add(tableJoinInfo.getJoinTable().joinType().getJoinString()
                    + " " + joinTableInfo.getTableName() + " " + strAlias);
            String joinColumnStr = Arrays.stream(tableJoinInfo.getJoinTable().joinColumns())
                    .map(x -> x.connector().convert((StringUtils.isBlank(masterTableName) ? "" : (masterTableName + ".")) + x.masterTableColumn(), x.value()))
                    .collect(Collectors.joining(" and "));
            joinTableList.add("on " + joinColumnStr);

            String joinColumn = Arrays.stream(joinTableInfo.getAllSqlSelect().split(","))
                    .map(x -> strAlias + "." + x ).collect(Collectors.joining(","));
            joinColumnList.add(joinColumn);
        });
        this.joinTables = joinTableList.stream().collect(Collectors.joining(" "));
        this.jointColumns = joinColumnList.stream().collect(Collectors.joining(",")).trim();
        this.jointColumns = (StringUtils.isBlank(this.jointColumns) ? "" : ",") + this.jointColumns;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<DtoField> getSubDtoFieldList() {
        return subDtoFieldList;
    }

    public List<DtoField> getNormalFieldList() {
        return normalFieldList;
    }

    public EntityDtoServiceRelation getEntityDtoServiceRelation() {
        return entityDtoServiceRelation;
    }

    public EntityClassInfo getEntityClassInfo() {
        return entityClassInfo;
    }

    public boolean containSubDto() {
        return this.subDtoFieldList.size() > 0;
    }

    public String getJoinTables() {
        return joinTables;
    }

    public String getJointColumns() {
        return jointColumns;
    }

    public DtoField getDtoField(String name) {
        if(dtoFieldMap.containsKey(name)) {
            return dtoFieldMap.get(name);
        }
        return null;
    }

    public DtoField getVersionField() {
        return versionField;
    }

    public Object getVersionDefaultValue() {
        return versionDefaultValue;
    }

    private Object getDefaultVersionByType(Class<?> clazz) {
        if(clazz == int.class) {
            return 1;
        } else if(clazz == Integer.class) {
            return 1;
        } else if(clazz == long.class) {
            return 1l;
        } else if(clazz == Long.class) {
            return 1l;
        } else if(clazz == Date.class) {
            return new Date();
        } else if(clazz == Timestamp.class) {
            return Timestamp.valueOf(LocalDateTime.now());
        } else if(clazz == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        return null;
    }

    public DtoField getKeyField() {
        return keyField;
    }

    public DtoField getDeleteFlagField() {
        return deleteFlagField;
    }
}
