package com.circustar.mybatis_accessor.class_info;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.dto.*;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class DtoField {
    private Field field;
    private EntityFieldInfo entityFieldInfo;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private DtoClassInfo dtoClassInfo;
    private QueryColumn queryColumn;
    private QueryWhere queryWhere;
    private QueryJoin queryJoin;
    private QueryGroupBy queryGroupBy;
    private QueryHaving queryHaving;
    private QueryOrder queryOrder;
    private List<Selector> selectors;
    private boolean idReference;

    private Boolean isCollection;
    private Class actualClass;
    private Class ownClass;

    private PropertyDescriptor propertyDescriptor;

    private TableJoinInfo tableJoinInfo;

    private DtoClassInfo fieldDtoClassInfo;

    private boolean deleteAndInsertNewOnUpdate;
    private boolean deleteEvenIfEmpty;
    private boolean updateCascade = true;

    public DtoField(PropertyDescriptor propertyDescriptor, EntityFieldInfo entityFieldInfo, DtoClassInfo dtoClassInfo, IEntityDtoServiceRelationMap relationMap) {
        this.entityFieldInfo = entityFieldInfo;
        this.dtoClassInfo = dtoClassInfo;

        this.propertyDescriptor = propertyDescriptor;
        this.field = FieldUtils.getField(dtoClassInfo.getDtoClass(), propertyDescriptor.getName());
        if (this.field == null) {
            return;
        }

        this.queryColumn = this.field.getAnnotation(QueryColumn.class);
        this.queryJoin = this.field.getAnnotation(QueryJoin.class);
        this.queryWhere = this.field.getAnnotation(QueryWhere.class);
        this.queryGroupBy = this.field.getAnnotation(QueryGroupBy.class);
        this.queryHaving = this.field.getAnnotation(QueryHaving.class);
        this.queryOrder = this.field.getAnnotation(QueryOrder.class);

        this.selectors = Arrays.stream(this.field.getAnnotationsByType(Selector.class))
                .sorted(Comparator.comparingInt(Selector::order))
                .collect(Collectors.toList());

        if(Collection.class.isAssignableFrom(this.field.getType())
                && this.field.getGenericType() instanceof ParameterizedType) {
            isCollection = true;
            actualClass = (Class) ((ParameterizedType) this.field.getGenericType()).getActualTypeArguments()[0];
            ownClass = (Class) ((ParameterizedType) this.field.getGenericType()).getRawType();
        } else {
            isCollection = false;
            actualClass = this.field.getType();
            ownClass = this.field.getType();
        }
        this.entityDtoServiceRelation = relationMap.getByDtoClass(actualClass);
        this.idReference = this.entityFieldInfo != null && this.entityFieldInfo.getIdReference() != null;
        this.retrieveDeleteAndInsertNewOnUpdate();
        this.retrieveUpdateCascade();

    }

    public EntityFieldInfo getEntityFieldInfo() {
        return entityFieldInfo;
    }

    public EntityDtoServiceRelation getEntityDtoServiceRelation() {
        return entityDtoServiceRelation;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public List<Selector> getSelectors() {
        return this.selectors;
    }

    public QueryColumn getQuerySelect() {
        return queryColumn;
    }

    public QueryJoin getQueryJoin() {
        return queryJoin;
    }

    public QueryWhere getQueryWhere() {
        return queryWhere;
    }

    public QueryGroupBy getQueryGroupBy() {
        return queryGroupBy;
    }

    public QueryHaving getQueryHaving() {
        return queryHaving;
    }

    public QueryOrder getQueryOrder() {
        return queryOrder;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Boolean getCollection() {
        return isCollection;
    }

    public Class getActualClass() {
        return actualClass;
    }

    public Class getOwnClass() {
        return ownClass;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public TableJoinInfo getTableJoinInfo() {
        return tableJoinInfo;
    }

    public void setTableJoinInfo(TableJoinInfo tableJoinInfo) {
        this.tableJoinInfo = tableJoinInfo;
    }

    public DtoClassInfo getFieldDtoClassInfo() {
        if(this.fieldDtoClassInfo == null) {
            this.fieldDtoClassInfo = this.getDtoClassInfo().getDtoClassInfoHelper().getDtoClassInfo(this.getActualClass());
        }
        return this.fieldDtoClassInfo;
    }

    public boolean isDeleteAndInsertNewOnUpdate() {
        return deleteAndInsertNewOnUpdate;
    }

    public boolean isDeleteEvenIfEmpty() { return deleteEvenIfEmpty; }

    private void retrieveDeleteAndInsertNewOnUpdate() {
        DeleteAndInsertNewOnUpdate deleteAndInsertAnnotation = this.field.getAnnotation(DeleteAndInsertNewOnUpdate.class);
        if(deleteAndInsertAnnotation != null) {
            this.deleteAndInsertNewOnUpdate = deleteAndInsertAnnotation.value();
            this.deleteEvenIfEmpty = deleteAndInsertAnnotation.deleteEvenIfEmpty();
        }
    }

    private void retrieveUpdateCascade() {
        UpdateCascade updateCascade = this.field.getAnnotation(UpdateCascade.class);
        if(updateCascade != null) {
            this.updateCascade = updateCascade.value();
        }
    }

    public boolean isIdReference() {
        return idReference;
    }

    public boolean isUpdateCascade() {
        return updateCascade;
    }

    enum SupportGenericType{
        LIST(List.class, ArrayList.class),
        COLLECTION(Collection.class, ArrayList.class),
        SET(Set.class, HashSet.class),
        QUEUE(Queue.class, PriorityQueue.class);
        private Class<? extends Collection> type;
        private Class<? extends Collection> newType;
        SupportGenericType(Class type, Class newType) {
            this.type = type;
            this.newType = newType;
        }
        public Class<? extends Collection> getOriginClass() {
            return this.type;
        }
        public Class<? extends Collection> getTargetClass() {
            return this.newType;
        }
        public static SupportGenericType getSupportGenericType(Class clazz) {
            return Arrays.stream(SupportGenericType.values()).filter(x -> x.getOriginClass() == clazz).findFirst().orElse(null);
        }
    }
}
