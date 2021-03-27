package com.circustar.mybatis_accessor.classInfo;

import com.circustar.mybatis_accessor.annotation.*;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

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

    private PropertyDescriptor propertyDescriptor;
    private Method readMethod;
    private Method writeMethod;

    public DtoField(Field field, EntityFieldInfo entityFieldInfo, DtoClassInfo dtoClassInfo, IEntityDtoServiceRelationMap relationMap) {
        this.field = field;
        this.entityFieldInfo = entityFieldInfo;
        this.dtoClassInfo = dtoClassInfo;

        this.propertyDescriptor = BeanUtils.getPropertyDescriptor(dtoClassInfo.getClazz(), field.getName());
        if(this.propertyDescriptor != null) {
            this.readMethod = this.propertyDescriptor.getReadMethod();
            this.writeMethod = this.propertyDescriptor.getWriteMethod();
        }

        this.querySelect = field.getAnnotation(QuerySelect.class);
        this.queryJoin = field.getAnnotation(QueryJoin.class);
        this.queryWhere = field.getAnnotation(QueryWhere.class);
        this.queryGroupBy = field.getAnnotation(QueryGroupBy.class);
        this.queryHaving = field.getAnnotation(QueryHaving.class);
        this.queryOrder = field.getAnnotation(QueryOrder.class);

        List<Selector> sortedSelectors = Arrays.stream(field.getAnnotationsByType(Selector.class))
                .sorted(Comparator.comparingInt(Selector::order))
                .collect(Collectors.toList());
        this.selectors = sortedSelectors.toArray(new Selector[sortedSelectors.size()]);

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

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
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
