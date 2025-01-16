package com.circustar.mybatis_accessor.class_info;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.ClassUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.*;
import com.circustar.mybatis_accessor.listener.event.decode.DecodeEventModel;
import com.circustar.mybatis_accessor.listener.event.update.UpdateEventModel;
import com.circustar.mybatis_accessor.annotation.dto.DeleteFlag;
import com.circustar.mybatis_accessor.listener.event.property_change.PropertyChangeEventModel;
import com.circustar.mybatis_accessor.converter.IConverter;
import com.circustar.mybatis_accessor.model.QueryJoinModel;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.AnnotationUtils;
import com.circustar.mybatis_accessor.model.QueryWrapperCreator;
import com.circustar.mybatis_accessor.utils.ApplicationContextUtils;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import com.circustar.mybatis_accessor.utils.TableJoinColumnPrefixManager;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DtoClassInfo {
    private final Class<?> clazz;
    private final DtoClassInfoHelper dtoClassInfoHelper;
    private final EntityDtoServiceRelation entityDtoServiceRelation;
    private final List<DtoField> subDtoFieldList;
    private final List<DtoField> updateCascadeDtoFieldList;
    private final List<DtoField> selectDtoFieldList;
    private final List<DtoField> normalFieldList;
    private final Map<String, DtoField> dtoFieldMap;
    private final EntityClassInfo entityClassInfo;
    private List<JoinExpression> joinExpressions;
    private DtoField versionField;
    private Object versionDefaultValue;
    private DtoField keyField;
    private DtoField deleteFlagField;
    private QueryWrapperCreator queryWrapperCreator;
    private List<UpdateEventModel> updateEventList;
    private List<PropertyChangeEventModel> propertyChangeEventList;
    private List<DecodeEventModel> decodeEventModelList;
    private DtoField idReferenceField;
    private IConverter convertDtoToEntity;
    private IConverter convertEntityToDto;
    private final IService serviceBean;

    public DtoClassInfo(IEntityDtoServiceRelationMap relationMap, DtoClassInfoHelper dtoClassInfoHelper, Class<?> clazz, EntityClassInfo entityClassInfo) {
        this.clazz = clazz;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
        this.entityDtoServiceRelation = relationMap.getByDtoClass(this.clazz);
        this.entityDtoServiceRelation.setDtoClassInfo(this);
        this.serviceBean = relationMap.getServiceBeanByDtoClass(this.clazz);
        this.entityClassInfo = entityClassInfo;
        this.subDtoFieldList = new ArrayList<>();
        this.normalFieldList = new ArrayList<>();
        this.dtoFieldMap = new HashMap<>();

        String versionPropertyName = null;
        if(entityClassInfo.getTableInfo().getVersionFieldInfo() != null) {
            versionPropertyName = entityClassInfo.getTableInfo().getVersionFieldInfo().getProperty();
        }
        String keyProperty = entityClassInfo.getTableInfo().getKeyProperty();
        String finalVersionPropertyName = versionPropertyName;
        List<PropertyDescriptor> propertyDescriptors = FieldUtils.getPropertyDescriptors(clazz);
        propertyDescriptors.stream().forEach(property -> {
            final Field fieldCheck = FieldUtils.getField(this.getDtoClass(), property.getName());
            if(fieldCheck == null) {
                return;
            }
            EntityFieldInfo entityFieldInfo = entityClassInfo.getFieldByName(property.getName());
            DtoField dtoField = new DtoField(property, entityFieldInfo, this, relationMap);
            if(property.getName().equals(keyProperty)) {
                this.keyField = dtoField;
            }
            DeleteFlag deleteFlagAnnotation = AnnotationUtils.getFieldAnnotation(dtoField.getField(), DeleteFlag.class);
            if(deleteFlagAnnotation != null) {
                this.deleteFlagField = dtoField;
            } else if(this.deleteFlagField == null && entityFieldInfo != null && entityFieldInfo.isLogicDeleteField()) {
                this.deleteFlagField = dtoField;
            }
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

        this.updateCascadeDtoFieldList = this.subDtoFieldList.stream().filter(x -> x.isUpdateCascade())
                .sorted(Comparator.comparingInt((DtoField x) -> x.getDtoClassInfo().getUpdateOrder())
                                .thenComparing(y -> y.getDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName())
                        ).collect(Collectors.toList());
        this.selectDtoFieldList = this.subDtoFieldList.stream().filter(x -> x.getQueryJoin() == null).collect(Collectors.toList());

        ApplicationContext applicationContext = dtoClassInfoHelper.getApplicationContext();
        this.initAfterUpdateList(applicationContext);
        this.initOnChangeList(applicationContext);
        this.initConverter(applicationContext);
        this.initDecodeEventList();
    }

    private void initConverter(ApplicationContext applicationContext) {
        this.convertDtoToEntity = ApplicationContextUtils.getBeanOrCreate(applicationContext
                , this.entityDtoServiceRelation.getConvertDtoToEntityClass());
        this.convertEntityToDto = ApplicationContextUtils.getBeanOrCreate(applicationContext
                , this.entityDtoServiceRelation.getConvertEntityToDtoClass());
    }

    private void initAfterUpdateList(ApplicationContext applicationContext) {
        MultiUpdateEvent multiUpdateEventAnnotation = this.clazz.getAnnotation(MultiUpdateEvent.class);
        List<UpdateEvent> var0 = null;
        if(multiUpdateEventAnnotation != null) {
            var0 = Arrays.asList(multiUpdateEventAnnotation.value());
        } else {
            UpdateEvent[] annotationsByType = this.clazz.getAnnotationsByType(UpdateEvent.class);
            if(annotationsByType!=null) {
                var0 = Arrays.asList(annotationsByType);
            }
        }
        if(var0!= null && !var0.isEmpty()) {
            this.updateEventList = var0.stream().map(x -> new UpdateEventModel(x.onExpression(), x.updateEventClass(),
                    () -> ApplicationContextUtils.getBeanOrCreate(applicationContext, x.updateEventClass())
                    , Arrays.asList(x.updateParams())
                    , Arrays.asList(x.updateType())
                    , x.executeTiming()))
                    .collect(Collectors.toList());
        }
    }

    private void initOnChangeList(ApplicationContext applicationContext) {
        MultiPropertyChangeEvent multiPropertyChangeEventAnnotation = this.clazz.getAnnotation(MultiPropertyChangeEvent.class);
        List<PropertyChangeEvent> var0 = null;
        if(multiPropertyChangeEventAnnotation != null) {
            var0 = Arrays.asList(multiPropertyChangeEventAnnotation.value());
        } else {
            PropertyChangeEvent[] annotationsByType = this.clazz.getAnnotationsByType(PropertyChangeEvent.class);
            if(annotationsByType!=null) {
                var0 = Arrays.asList(annotationsByType);
            }
        }
        if(var0!= null && !var0.isEmpty()) {
            this.propertyChangeEventList = var0.stream().map(x -> new PropertyChangeEventModel(x.fromExpression(),
                    x.toExpression(),
                    Arrays.asList(x.listenProperties()),
                    x.updateEventClass(),
                    () -> ApplicationContextUtils.getBeanOrCreate(applicationContext, x.updateEventClass())
                    , Arrays.asList(x.updateParams())
                    , Arrays.asList(x.updateType())
                    , x.executeTiming()))
                    .collect(Collectors.toList());
        }
    }

    private void initDecodeEventList() {
        MultiDecodeEvent multiDecodeEventAnnotation = this.clazz.getAnnotation(MultiDecodeEvent.class);
        List<DecodeEvent> var0 = null;
        if(multiDecodeEventAnnotation != null) {
            var0 = Arrays.asList(multiDecodeEventAnnotation.value());
        } else {
            DecodeEvent[] annotationsByType = this.clazz.getAnnotationsByType(DecodeEvent.class);
            if(annotationsByType!=null) {
                var0 = Arrays.asList(annotationsByType);
            }
        }
        if(var0!= null && !var0.isEmpty()) {
            this.decodeEventModelList = var0.stream().map(x -> new DecodeEventModel(this
                    , x.onExpression()
                    , x.targetProperties(), x.matchProperties()
                    , x.sourceDtoClass(), x.sourceProperties(), x.matchSourceProperties()
                    , x.errorWhenNotExist()
                    , x.updateType()
                    , x.executeTiming()))
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

        this.joinExpressions = tableJoinInfoList.stream().sorted(Comparator.comparingInt(x -> x.getQueryJoin().getOrder()))
                .map(tableJoinInfo -> {
                    Class joinClazz = tableJoinInfo.getActualClass();
                    TableInfo joinTableInfo = TableInfoHelper.getTableInfo(joinClazz);
                    JoinExpression joinExpression = new JoinExpression(tableJoinInfo.getFieldName());
                    String connectString = tableJoinInfo.getQueryJoin().getJoinType().getJoinExpression()
                            + " " + getJoinTableName(joinTableInfo.getTableName(), tableJoinInfo.getQueryJoin()) + " " + tableJoinInfo.getQueryJoin().getTableAlias()
                            + " on ";
                    if(!org.springframework.util.StringUtils.hasLength(tableJoinInfo.getQueryJoin().getJoinExpression())) {
                        if(this.entityClassInfo.getFieldByName(joinTableInfo.getKeyProperty()) != null) {
                            connectString += this.entityClassInfo.getTableInfo().getTableName() + "." + joinTableInfo.getKeyColumn()
                                    + " = " + tableJoinInfo.getQueryJoin().getTableAlias() + "." + joinTableInfo.getKeyColumn();
                        } else {
                            connectString += this.entityClassInfo.getTableInfo().getTableName() + "." + this.entityClassInfo.getTableInfo().getKeyColumn()
                                    + " = " + tableJoinInfo.getQueryJoin().getTableAlias() + "." + this.entityClassInfo.getTableInfo().getKeyColumn();
                        }
                    } else {
                        connectString += tableJoinInfo.getQueryJoin().getJoinExpression();
                    }
                    joinExpression.setJoinString(connectString);
                    joinExpression.setColumnNames(Arrays.stream(joinTableInfo.getAllSqlSelect().split(","))
                            .map(x -> tableJoinInfo.getQueryJoin().getTableAlias() + "." + x
                                    + " as " + TableJoinColumnPrefixManager.tryGet(entityClassInfo.getEntityClass()
                                    , tableJoinInfo.getActualClass(), tableJoinInfo.getPosition()) + "_" + x )
                            .collect(Collectors.toList()));
                    return joinExpression;
                }).collect(Collectors.toList());
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

    public List<DtoField> getSelectDtoFieldList() {
        return selectDtoFieldList;
    }


    public List<DtoField> getNormalFieldList() {
        return normalFieldList;
    }

    public Collection<DtoField> getAllFieldList() {
        return dtoFieldMap.values();
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

    public List<JoinExpression> getJoinExpressions() {
        return joinExpressions;
    }

    public void setJoinExpressions(List<JoinExpression> joinExpressions) {
        this.joinExpressions = joinExpressions;
    }

    public String getJoinString(List<String> joinTableNames) {
        if(CollectionUtils.isEmpty(this.joinExpressions)) {
            return "";
        }
        if(CollectionUtils.isEmpty(joinTableNames)) {
            return joinExpressions.stream().map(x -> x.getJoinString()).collect(Collectors.joining(" "));
        }
        return joinExpressions.stream().filter(x -> joinTableNames.stream().anyMatch(y -> x.getJoinName().equals(y)))
                .map(x -> x.getJoinString()).collect(Collectors.joining(" "));
    }

    public String getJoinColumns(List<String> joinTableNames) {
        if(CollectionUtils.isEmpty(this.joinExpressions)) {
            return "";
        }
        if(CollectionUtils.isEmpty(joinTableNames)) {
            return joinExpressions.stream().map(x -> x.getColumnNames()).flatMap(List::stream)
                    .collect(Collectors.joining(","));
        }
        return joinExpressions.stream().filter(x -> joinTableNames.stream().anyMatch(y -> x.getJoinName().equals(y)))
                .map(x -> x.getColumnNames()).flatMap(List::stream).collect(Collectors.joining(","));
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

    public DtoClassInfoHelper getDtoClassInfoHelper() {
        return dtoClassInfoHelper;
    }

    public IService getServiceBean() {
        return this.serviceBean;
    }

    public List<UpdateEventModel> getUpdateEventList() {
        return updateEventList;
    }

    public List<PropertyChangeEventModel> getPropertyChangeEventList() {
        return propertyChangeEventList;
    }

    public List<DecodeEventModel> getDecodeEventModelList() {
        return decodeEventModelList;
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

    public static List<DtoField> getDtoFieldsByName(DtoClassInfo dtoClassInfo
            , boolean includeAllChildren, boolean isForUpdate, List<String> childNames) {
        if(includeAllChildren) {
            if(isForUpdate) {
                return dtoClassInfo.getUpdateCascadeDtoFieldList();
            } else {
                return dtoClassInfo.getSelectDtoFieldList();
            }
        }
        if(CollectionUtils.isEmpty(childNames)) {
            return Collections.emptyList();
        }
        List<DtoField> result = childNames.stream().map(x -> dtoClassInfo.getDtoField(x)).collect(Collectors.toList());
        if(isForUpdate) {
            result = result.stream().sorted(Comparator.comparingInt((DtoField x) -> x.getDtoClassInfo().getUpdateOrder())
                    .thenComparing(y -> y.getDtoClassInfo().getEntityClassInfo().getEntityClass().getSimpleName())
            ).collect(Collectors.toList());
        }
        return result;
    }

    public Object createInstance() {
        return ClassUtils.createInstance(this.getDtoClass());
    }

    public static int equalPropertiesIgnoreEmpty(DtoClassInfo dtoClassInfo, Object newObj, Object oldObj, List<String> propertyNames) {
        if(newObj == oldObj) {
            return 1;
        }
        boolean partEqual = false;
        boolean allEqual = true;
        for(String propertyName : propertyNames) {
            DtoField field = dtoClassInfo.getDtoField(propertyName);
            Object newVal = null;
            if(newObj != null) {
                newVal = FieldUtils.getFieldValue(newObj, field.getPropertyDescriptor().getReadMethod());
            }
            Object oldVal = null;
            if(oldObj != null) {
                oldVal = FieldUtils.getFieldValue(oldObj, field.getPropertyDescriptor().getReadMethod());
            }
            if(newVal == null) {
                continue;
            } else if(oldVal == null) {
                if(newVal instanceof String && StringUtils.isBlank(newVal.toString())) {
                    continue;
                } else {
                    allEqual = false;
                }
            } else if(!newVal.equals(oldVal)) {
                allEqual = false;
            } else {
                partEqual = true;
            }
        }
        return allEqual ? 1 : (partEqual ? 0 : -1);
    }

    public static String getJoinTableName(String tableName, QueryJoinModel model) {
        String subQueryExpression = model.getSubQueryExpression();
        if(StringUtils.isBlank(subQueryExpression)) {
            return tableName;
        }
        return "(" + model.getSubQueryExpression() + ")";

    }
}
