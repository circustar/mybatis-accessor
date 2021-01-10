package com.circustar.mvcenhance.enhance.field;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.mvcenhance.common.query.EntityFilter;
import com.circustar.mvcenhance.common.query.QueryField;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.util.*;
import java.util.stream.Collectors;

public class DtoField {
    private String fieldName;
    private FieldTypeInfo fieldTypeInfo;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private DtoClassInfo dtoClassInfo;
    private List<QueryField> queryFields;
    private Map<String, Set<QueryField>> queryFieldsByGroup = new HashMap<>();
    private List<EntityFilter> entityFilters;
    private Class relatedEntityClass = null;
    private Boolean hasEntityClass = null;

    public DtoField(String fieldName,FieldTypeInfo fieldTypeInfo, DtoClassInfo dtoClassInfo, EntityDtoServiceRelation entityDtoServiceRelation) {
        this.fieldName = fieldName;
        this.fieldTypeInfo = fieldTypeInfo;
        this.dtoClassInfo = dtoClassInfo;
        this.entityDtoServiceRelation = entityDtoServiceRelation;

        QueryField[] queryFields = fieldTypeInfo.getField().getAnnotationsByType(QueryField.class);
        this.queryFields = Arrays.asList(queryFields);
        EntityFilter[] entityFilters = fieldTypeInfo.getField().getAnnotationsByType(EntityFilter.class);
        this.entityFilters = Arrays.asList(entityFilters);

        for(QueryField queryField : queryFields) {
            Set<QueryField> defaultQueryFieldSet = null;
            if(queryFieldsByGroup.containsKey("")) {
                defaultQueryFieldSet = queryFieldsByGroup.get("");
            } else {
                defaultQueryFieldSet = new HashSet<>();
                queryFieldsByGroup.put("", defaultQueryFieldSet);
            }
            defaultQueryFieldSet.add(queryField);
            Arrays.stream(queryField.group()).filter(x -> !StringUtils.isBlank(x)).forEach(x -> {
                Set<QueryField> queryFieldSet = null;
                if(queryFieldsByGroup.containsKey(x)) {
                    queryFieldSet = queryFieldsByGroup.get(x);
                } else {
                    queryFieldSet = new HashSet<>();
                    queryFieldsByGroup.put(x, queryFieldSet);
                }
                queryFieldSet.add(queryField);
            });
        }
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

    public List<QueryField> getQueryFields() {
        return queryFields;
    }

    public Set<QueryField> getQueryField(String gorupName) {
        if(queryFieldsByGroup.containsKey(gorupName)) {
            return queryFieldsByGroup.get(gorupName);
        }
        return null;
    }

    public List<EntityFilter> getEntityFilters() {
        return entityFilters;
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
