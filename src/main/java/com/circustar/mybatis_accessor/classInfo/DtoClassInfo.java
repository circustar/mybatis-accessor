package com.circustar.mybatis_accessor.classInfo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.listener.UpdateListener;
import com.circustar.mybatis_accessor.annotation.listener.UpdateEventModel;
import com.circustar.mybatis_accessor.annotation.listener.MultiUpdateListener;
import com.circustar.mybatis_accessor.annotation.dto.DeleteFlag;
import com.circustar.mybatis_accessor.annotation.listener.property_change.MultiPropertyChangeListener;
import com.circustar.mybatis_accessor.annotation.listener.property_change.PropertyChangeListener;
import com.circustar.mybatis_accessor.annotation.listener.property_change.PropertyChangeEventModel;
import com.circustar.mybatis_accessor.converter.IConverter;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.AnnotationUtils;
import com.circustar.mybatis_accessor.model.QueryWrapperCreator;
import com.circustar.mybatis_accessor.utils.ApplicationContextUtils;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import com.circustar.mybatis_accessor.utils.TableJoinColumnPrefixManager;
import org.springframework.context.ApplicationContext;

import java.beans.PropertyDescriptor;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DtoClassInfo {
    private Class<?> clazz;
    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;
    private DtoClassInfoHelper dtoClassInfoHelper;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private List<DtoField> subDtoFieldList;
    private List<DtoField> updateCascadeDtoFieldList;
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
    private List<UpdateEventModel> updateEventList;
    private List<PropertyChangeEventModel> propertyChangeEventList;
    private DtoField idReferenceField;
    private IConverter convertDtoToEntity;
    private IConverter convertEntityToDto;
    private boolean containUpdateField;

    public DtoClassInfo(IEntityDtoServiceRelationMap relationMap, DtoClassInfoHelper dtoClassInfoHelper, Class<?> clazz, EntityClassInfo entityClassInfo) {
        this.clazz = clazz;
        this.entityDtoServiceRelationMap = relationMap;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
        this.entityDtoServiceRelation = this.entityDtoServiceRelationMap.getByDtoClass(this.clazz);
        this.entityDtoServiceRelation.setDtoClassInfo(this);
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
            } else if(this.deleteFlagField == null && entityFieldInfo != null && entityFieldInfo.isLogicDeleteField()) {
                this.deleteFlagField = dtoField;
            }
            this.allFieldList.add(dtoField);
            this.dtoFieldMap.put(property.getName(), dtoField);
            if(dtoField.getEntityDtoServiceRelation() != null) {
                subDtoFieldList.add(dtoField);
            } else if(TableInfoUtils.isMybatisSupportType(dtoField.getActualClass())) {
                normalFieldList.add(dtoField);
                if(property.getName().equals(finalVersionPropertyName)) {
                    this.versionField = dtoField;
                    this.versionDefaultValue = getDefaultVersionByType(entityFieldInfo.getField().getType());
                }
            }
            if(dtoField.isIdReference()) {
                this.idReferenceField = dtoField;
            }
        });

        this.updateCascadeDtoFieldList = this.subDtoFieldList.stream().filter(x -> x.isUpdateCascade()).collect(Collectors.toList());
        this.containUpdateField = this.normalFieldList.stream().filter(x -> x!= keyField && x != versionField
                && x.getEntityFieldInfo() != null
                && (x.getEntityFieldInfo().getTableField() == null || x.getEntityFieldInfo().getTableField().exist()))
                .findAny().isPresent();

        ApplicationContext applicationContext = dtoClassInfoHelper.getApplicationContext();
        initAfterUpdateList(applicationContext);
        initOnChangeList(applicationContext);
        initConverter(applicationContext);
    }

    protected void initConverter(ApplicationContext applicationContext) {
        this.convertDtoToEntity = ApplicationContextUtils.getBeanOrCreate(applicationContext
                , this.entityDtoServiceRelation.getConvertDtoToEntityClass());
        this.convertEntityToDto = ApplicationContextUtils.getBeanOrCreate(applicationContext
                , this.entityDtoServiceRelation.getConvertEntityToDtoClass());
    }

    protected void initAfterUpdateList(ApplicationContext applicationContext) {
        MultiUpdateListener multiUpdateListenerAnnotation = this.clazz.getAnnotation(MultiUpdateListener.class);
        List<UpdateListener> var0 = null;
        if(multiUpdateListenerAnnotation != null) {
            var0 = Arrays.asList(multiUpdateListenerAnnotation.value());
        } else {
            UpdateListener[] annotationsByType = this.clazz.getAnnotationsByType(UpdateListener.class);
            if(annotationsByType!=null) {
                var0 = Arrays.asList(annotationsByType);
            }
        }
        if(var0!= null & !var0.isEmpty()) {
            this.updateEventList = var0.stream().map(x -> new UpdateEventModel(x.onExpression(),
                    () -> ApplicationContextUtils.getBeanOrCreate(applicationContext, x.afterUpdateExecutor())
                    , x.updateParams()))
                    .collect(Collectors.toList());
        }
    }

    protected void initOnChangeList(ApplicationContext applicationContext) {
        MultiPropertyChangeListener multiPropertyChangeListenerAnnotation = this.clazz.getAnnotation(MultiPropertyChangeListener.class);
        List<PropertyChangeListener> var0 = null;
        if(multiPropertyChangeListenerAnnotation != null) {
            var0 = Arrays.asList(multiPropertyChangeListenerAnnotation.value());
        } else {
            PropertyChangeListener[] annotationsByType = this.clazz.getAnnotationsByType(PropertyChangeListener.class);
            if(annotationsByType!=null) {
                var0 = Arrays.asList(annotationsByType);
            }
        }
        if(var0!= null & !var0.isEmpty()) {
            this.propertyChangeEventList = var0.stream().map(x -> new PropertyChangeEventModel(x.changeProperties(),
                    x.triggerOnAnyChanged(),
                    () -> ApplicationContextUtils.getBeanOrCreate(applicationContext, x.onChangeExecutor())
                    , x.updateParams()))
                    .collect(Collectors.toList());
        }
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

    public Class<?> getDtoClass() {
        return clazz;
    }

    public List<DtoField> getSubDtoFieldList() {
        return subDtoFieldList;
    }

    public List<DtoField> getUpdateCascadeDtoFieldList() {
        return updateCascadeDtoFieldList;
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

    public <T> QueryWrapper<T> createQueryWrapper(Object dto) {
        if(this.queryWrapperCreator == null) {
            this.queryWrapperCreator = new QueryWrapperCreator(this);
        }
        return this.queryWrapperCreator.createQueryWrapper(dto);
    }

    public int getUpdateOrder() {
        return this.entityClassInfo.getUpdateOrder();
    }

    public boolean isObjectDeleted(Object dto) {
        if(this.getDeleteFlagField() == null) {return false;}
        Object deleteFlagValue = FieldUtils.getFieldValue(dto, this.getDeleteFlagField().getPropertyDescriptor().getReadMethod());
        if(deleteFlagValue == null) {
            return false;
        }
        if(deleteFlagValue instanceof Boolean) {
            return (Boolean)deleteFlagValue;
        }
        return !"0".equals(deleteFlagValue.toString());
    }

    public IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        return entityDtoServiceRelationMap;
    }

    public DtoClassInfoHelper getDtoClassInfoHelper() {
        return dtoClassInfoHelper;
    }

    public IService getServiceBean() {
        return getEntityDtoServiceRelationMap().getServiceBeanByDtoClass(this.clazz);
    }

    public List<UpdateEventModel> getUpdateEventList() {
        return updateEventList;
    }

    public List<PropertyChangeEventModel> getPropertyChangeEventList() {
        return propertyChangeEventList;
    }

    public DtoField getIdReferenceField() {
        return idReferenceField;
    }

    public IConverter getConvertDtoToEntity() {
        return convertDtoToEntity;
    }

    public IConverter getConvertEntityToDto() {
        return convertEntityToDto;
    }

    public Object convertToEntity(Object dto) {
        return this.convertDtoToEntity.convert(this.getEntityClassInfo().getEntityClass(), dto);
    }

    public Object convertFromEntity(Object entity) {
        return this.convertEntityToDto.convert(this.getDtoClass(), entity);
    }

    public boolean isContainUpdateField() {
        return containUpdateField;
    }

    public static int equalProperties(DtoClassInfo dtoClassInfo, Object obj1, Object obj2, String[] propertyNames) {
        boolean partEqual = false;
        boolean allEqual = true;
        try {
            if(obj1 == obj2) {
                return 1;
            } else if(obj1 == null && obj2 != null) {
                return -1;
            } else if(obj1 != null && obj2 == null) {
                return -1;
            }
            for(String propertyName : propertyNames) {
                DtoField field = dtoClassInfo.getDtoField(propertyName);
                Object val1 = FieldUtils.getFieldValue(obj1, field.getPropertyDescriptor().getReadMethod());
                Object val2 = FieldUtils.getFieldValue(obj2, field.getPropertyDescriptor().getReadMethod());
                if(val1 == null && val2 != null) {
                    allEqual = false;
                } else if(val1 != null && val2 == null) {
                    allEqual = false;
                } else if(val1 != null && val2 != null && !val1.equals(val2)) {
                    allEqual = false;
                } else {
                    partEqual = true;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return allEqual ? 1 : (partEqual ? 0 : -1);
    }
}
