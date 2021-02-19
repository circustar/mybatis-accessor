package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.mvcenhance.annotation.GroupField;
import com.circustar.mvcenhance.annotation.OrderField;
import com.circustar.mvcenhance.annotation.Selector;
import com.circustar.mvcenhance.annotation.QueryField;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import org.springframework.core.annotation.Order;

import java.util.*;

public class DtoField {
    private String fieldName;
    private FieldTypeInfo fieldTypeInfo;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private DtoClassInfo dtoClassInfo;
    private QueryField queryField;
    private Selector selector;
    private OrderField orderField;
    private GroupField groupField;
    private Class relatedEntityClass = null;
    private Boolean hasEntityClass = null;

    public DtoField(String fieldName,FieldTypeInfo fieldTypeInfo, DtoClassInfo dtoClassInfo, EntityDtoServiceRelation entityDtoServiceRelation) {
        this.fieldName = fieldName;
        this.fieldTypeInfo = fieldTypeInfo;
        this.dtoClassInfo = dtoClassInfo;
        this.entityDtoServiceRelation = entityDtoServiceRelation;

        this.queryField = fieldTypeInfo.getField().getAnnotation(QueryField.class);
        this.selector = fieldTypeInfo.getField().getAnnotation(Selector.class);
        this.orderField = fieldTypeInfo.getField().getAnnotation(OrderField.class);
        this.groupField = fieldTypeInfo.getField().getAnnotation(GroupField.class);
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

    public Selector getSelector() {
        return selector;
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
}
