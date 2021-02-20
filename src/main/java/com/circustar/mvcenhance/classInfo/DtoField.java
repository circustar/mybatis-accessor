package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.mvcenhance.annotation.*;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import org.springframework.core.annotation.Order;

import java.util.*;

public class DtoField {
    private String fieldName;
    private FieldTypeInfo fieldTypeInfo;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private DtoClassInfo dtoClassInfo;
    private QueryField queryField;
    private OrderField orderField;
    private GroupField groupField;
    private Selector[] selectors;
    private Class relatedEntityClass = null;
    private Boolean hasEntityClass = null;
    private JoinTable joinTable;

    public DtoField(String fieldName,FieldTypeInfo fieldTypeInfo, DtoClassInfo dtoClassInfo, EntityDtoServiceRelation entityDtoServiceRelation) {
        this.fieldName = fieldName;
        this.fieldTypeInfo = fieldTypeInfo;
        this.dtoClassInfo = dtoClassInfo;
        this.entityDtoServiceRelation = entityDtoServiceRelation;

        this.queryField = fieldTypeInfo.getField().getAnnotation(QueryField.class);
        this.selectors = fieldTypeInfo.getField().getAnnotationsByType(Selector.class);
        this.orderField = fieldTypeInfo.getField().getAnnotation(OrderField.class);
        this.groupField = fieldTypeInfo.getField().getAnnotation(GroupField.class);
        this.joinTable = fieldTypeInfo.getField().getAnnotation(JoinTable.class);
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldTypeInfo getFieldTypeInfo() {
        return fieldTypeInfo;
    }

    public EntityDtoServiceRelation getEntityDtoServiceRelation() {
        return entityDtoServiceRelation;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public QueryField getQueryField() {
        return queryField;
    }

    public Selector[] getSelectors() {
        return selectors;
    }

    public OrderField getOrderField() {
        return orderField;
    }

    public GroupField getGroupField() {
        return groupField;
    }

    public Boolean getHasEntityClass() {
        return hasEntityClass;
    }

    public void setHasEntityClass(Boolean hasEntityClass) {
        this.hasEntityClass = hasEntityClass;
    }

    public Class getRelatedEntityClass() {
        return relatedEntityClass;
    }

    public void setRelatedEntityClass(Class relatedEntityClass) {
        this.relatedEntityClass = relatedEntityClass;
    }

    public JoinTable getJoinTable() {
        return joinTable;
    }

    enum SupportGenericType{
        list(List.class, ArrayList.class),
        collection(Collection.class, ArrayList.class),
        set(Set.class, HashSet.class),
        queue(Queue.class, PriorityQueue.class);
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
        public static SupportGenericType getSupportGenericType(Class t) {
            return Arrays.stream(SupportGenericType.values()).filter(x -> x.getOriginClass() == t).findFirst().orElse(null);
        }
    }
}
