package com.circustar.mybatis_accessor.classInfo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.DeleteFlag;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.AnnotationUtils;
import com.circustar.mybatis_accessor.model.QueryWrapperCreator;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import com.circustar.mybatis_accessor.utils.TableJoinColumnPrefixManager;

import java.beans.PropertyDescriptor;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DtoClassInfo {
    private Class<?> clazz;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private List<DtoField> subDtoFieldList;
    private List<DtoField> normalFieldList;
    private List<DtoField> allFieldList;
    private Map<String, DtoField> dtoFieldMap;
    private EntityClassInfo entityClassInfo;
    private String joinTables;
    private String joinColumns;
    private DtoField versionField;
    private Object versionDefaultValue;
    private DtoField keyField;
    private DtoField deleteFlagField;
    private boolean physicDelete = false;
    private QueryWrapperCreator queryWrapperCreator;
    public DtoClassInfo(IEntityDtoServiceRelationMap relationMap, Class<?> clazz, EntityClassInfo entityClassInfo) {
        this.clazz = clazz;
        this.entityDtoServiceRelation = relationMap.getByDtoClass(this.clazz);
        this.entityClassInfo = entityClassInfo;
        this.subDtoFieldList = new ArrayList<>();
        this.normalFieldList = new ArrayList<>();
        this.allFieldList = new ArrayList<>();
        this.dtoFieldMap = new HashMap<>();

        String versionPropertyName = null;
        if(entityClassInfo.getTableInfo().getVersionFieldInfo() != null) {
            versionPropertyName = entityClassInfo.getTableInfo().getVersionFieldInfo().getProperty();
        }
        String keyProperty = entityClassInfo.getTableInfo().getKeyProperty();
        String finalVersionPropertyName = versionPropertyName;
        List<PropertyDescriptor> propertyDescriptors = FieldUtils.getPropertyDescriptors(clazz);
        propertyDescriptors.stream().forEach(property -> {
            EntityFieldInfo entityFieldInfo = entityClassInfo.getFieldByName(property.getName());
            DtoField dtoField = new DtoField(property, entityFieldInfo, this, relationMap);
            if(property.getName().equals(keyProperty)) {
                this.keyField = dtoField;
            }
            DeleteFlag deleteFlagAnnotation = AnnotationUtils.getFieldAnnotation(dtoField.getField(), DeleteFlag.class);
            if(deleteFlagAnnotation != null) {
                this.deleteFlagField = dtoField;
                this.physicDelete = deleteFlagAnnotation.physicDelete();
            }
            this.allFieldList.add(dtoField);
            this.dtoFieldMap.put(property.getName(), dtoField);
            if(dtoField.getEntityDtoServiceRelation() != null) {
                subDtoFieldList.add(dtoField);
            } else if(TableInfoUtils.isMybatisSupportType((Class)dtoField.getActualClass())) {
                normalFieldList.add(dtoField);
                if(property.getName().equals(finalVersionPropertyName)) {
                    this.versionField = dtoField;
                    this.versionDefaultValue = getDefaultVersionByType(entityFieldInfo.getField().getType());
                }
            }
        });
    }

    public void initJoinTableInfo(DtoClassInfoHelper dtoClassInfoHelper) {
        this.subDtoFieldList.stream().forEach(x -> {
            TableJoinInfo tableJoinInfo = TableJoinInfo.parseDtoFieldJoinInfo(dtoClassInfoHelper, this.clazz, x);
            x.setTableJoinInfo(tableJoinInfo);
        });
        List<TableJoinInfo> tableJoinInfoList = this.subDtoFieldList.stream().map(x -> x.getTableJoinInfo()).filter(x -> x != null).collect(Collectors.toList());
        TableJoinInfo.setPosition(tableJoinInfoList);

        List<String> joinTableList = new ArrayList<>();
        List<String> joinColumnList = new ArrayList<>();
        tableJoinInfoList.stream().sorted(Comparator.comparingInt(x -> x.getQueryJoin().getOrder()))
                .forEach(tableJoinInfo -> {
                    Class joinClazz = tableJoinInfo.getActualClass();
                    TableInfo joinTableInfo = TableInfoHelper.getTableInfo(joinClazz);
                    joinTableList.add(tableJoinInfo.getQueryJoin().getJoinType().getJoinExpression()
                            + " " + joinTableInfo.getTableName() + " " + tableJoinInfo.getQueryJoin().getTableAlias());
                    String joinExpression = tableJoinInfo.getQueryJoin().getJoinExpression();
                    if(org.springframework.util.StringUtils.isEmpty(joinExpression)) {
                        if(this.entityClassInfo.getFieldByName(joinTableInfo.getKeyProperty()) != null) {
                            joinExpression = this.entityClassInfo.getTableInfo().getTableName() + "." + joinTableInfo.getKeyColumn()
                                    + " = " + tableJoinInfo.getQueryJoin().getTableAlias() + "." + joinTableInfo.getKeyColumn();
                        } else {
                            joinExpression = this.entityClassInfo.getTableInfo().getTableName() + "." + this.entityClassInfo.getTableInfo().getKeyColumn()
                                    + " = " + tableJoinInfo.getQueryJoin().getTableAlias() + "." + this.entityClassInfo.getTableInfo().getKeyColumn();
                        }
                    }
                    joinTableList.add(" on " + joinExpression);

                    String joinColumn = Arrays.stream(joinTableInfo.getAllSqlSelect().split(","))
                            .map(x -> tableJoinInfo.getQueryJoin().getTableAlias() + "." + x
                                    + " as " + TableJoinColumnPrefixManager.tryGet(entityClassInfo.getEntityClass()
                                    , tableJoinInfo.getActualClass(), tableJoinInfo.getPosition()) + "_" + x )
                            .collect(Collectors.joining(","));
                    joinColumnList.add(joinColumn);
                });
        this.joinTables = joinTableList.stream().collect(Collectors.joining(" "));
        this.joinColumns = joinColumnList.stream().collect(Collectors.joining(",")).trim();
        this.joinColumns = (StringUtils.isBlank(this.joinColumns) ? "" : ",") + this.joinColumns;
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

    public List<DtoField> getAllFieldList() {
        return allFieldList;
    }

    public EntityDtoServiceRelation getEntityDtoServiceRelation() {
        return entityDtoServiceRelation;
    }

    public EntityClassInfo getEntityClassInfo() {
        return entityClassInfo;
    }

    public boolean containSubDto() {
        return !this.subDtoFieldList.isEmpty();
    }

    public String getJoinTables() {
        return joinTables;
    }

    public String getJoinColumns() {
        return joinColumns;
    }

    public DtoField getDtoField(String name) {
        return dtoFieldMap.get(name);
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

    public boolean isPhysicDelete() {
        return physicDelete;
    }

    public <T> QueryWrapper<T> createQueryWrapper(DtoClassInfoHelper dtoClassInfoHelper, Object dto) {
        if(this.queryWrapperCreator == null) {
            this.queryWrapperCreator = new QueryWrapperCreator(dtoClassInfoHelper, this);
        }
        return this.queryWrapperCreator.createQueryWrapper(dto);
    }
}
