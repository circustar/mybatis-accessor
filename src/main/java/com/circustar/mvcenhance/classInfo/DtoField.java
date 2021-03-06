package com.circustar.mvcenhance.classInfo;

import com.circustar.mvcenhance.annotation.*;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class DtoField {
    private Field field;
    private EntityFieldInfo entityFieldInfo;
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

    private Boolean isCollection = false;
    private Type actualType = null;
    private Type ownType = null;

    public DtoField(Field field, EntityFieldInfo entityFieldInfo, DtoClassInfo dtoClassInfo, IEntityDtoServiceRelationMap relationMap) {
        this.field = field;
        this.entityFieldInfo = entityFieldInfo;
        this.dtoClassInfo = dtoClassInfo;

        this.querySelect = field.getAnnotation(QuerySelect.class);
        this.queryJoin = field.getAnnotation(QueryJoin.class);
        this.queryWhere = field.getAnnotation(QueryWhere.class);
        this.queryGroupBy = field.getAnnotation(QueryGroupBy.class);
        this.queryHaving = field.getAnnotation(QueryHaving.class);
        this.queryOrder = field.getAnnotation(QueryOrder.class);

        this.selectors = field.getAnnotationsByType(Selector.class);

        if(Collection.class.isAssignableFrom(field.getType())
                && field.getGenericType() instanceof ParameterizedType) {
            isCollection = true;
            actualType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            ownType = ((ParameterizedType) field.getGenericType()).getRawType();
        } else {
            isCollection = false;
            actualType = field.getType();
            ownType = field.getType();
        }
        this.entityDtoServiceRelation = relationMap.getByDtoClass((Class)actualType);
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

    public Class getRelatedEntityClass() {
        return relatedEntityClass;
    }

    public void setRelatedEntityClass(Class relatedEntityClass) {
        this.relatedEntityClass = relatedEntityClass;
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

    public Type getActualType() {
        return actualType;
    }

    public Type getOwnType() {
        return ownType;
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
