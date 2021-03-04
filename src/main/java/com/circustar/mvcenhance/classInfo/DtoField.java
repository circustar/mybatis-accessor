package com.circustar.mvcenhance.classInfo;

import com.circustar.mvcenhance.annotation.*;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;

import java.util.*;

public class DtoField {
    private String fieldName;
    private TableFieldInfo tableFieldInfo;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private DtoClassInfo dtoClassInfo;
    private QuerySelect querySelect;
    private QueryWhere queryWhere;
    private QueryJoin queryJoin;
    private QueryGroupBy queryGroupBy;
    private QueryHaving queryHaving;
    private QueryOrder queryOrder;
    private Selector[] selectors;
    private Class relatedEntityClass = null;
    private Boolean hasEntityClass = null;

    public DtoField(String fieldName, TableFieldInfo tableFieldInfo, DtoClassInfo dtoClassInfo, EntityDtoServiceRelation entityDtoServiceRelation) {
        this.fieldName = fieldName;
        this.tableFieldInfo = tableFieldInfo;
        this.dtoClassInfo = dtoClassInfo;
        this.entityDtoServiceRelation = entityDtoServiceRelation;

        this.querySelect = tableFieldInfo.getField().getAnnotation(QuerySelect.class);
        this.queryJoin = tableFieldInfo.getField().getAnnotation(QueryJoin.class);
        this.queryWhere = tableFieldInfo.getField().getAnnotation(QueryWhere.class);
        this.queryGroupBy = tableFieldInfo.getField().getAnnotation(QueryGroupBy.class);
        this.queryHaving = tableFieldInfo.getField().getAnnotation(QueryHaving.class);
        this.queryOrder = tableFieldInfo.getField().getAnnotation(QueryOrder.class);

        this.selectors = tableFieldInfo.getField().getAnnotationsByType(Selector.class);
    }

    public String getFieldName() {
        return fieldName;
    }

    public TableFieldInfo getTableFieldInfo() {
        return tableFieldInfo;
    }

    public EntityDtoServiceRelation getEntityDtoServiceRelation() {
        return entityDtoServiceRelation;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public Selector[] getSelectors() {
        return selectors;
    }

    public QuerySelect getQuerySelect() {
        return querySelect;
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
