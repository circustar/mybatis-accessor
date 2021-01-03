package com.circustar.mvcenhance.enhance.field;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DtoClassInfo {
    private Class<?> clazz;
    private IEntityDtoServiceRelationMap relationMap;
    private EntityDtoServiceRelation entityDtoServiceRelation;
    private List<DtoField> dtoFieldList;
    private List<FieldTypeInfo> normalFieldList;
    private EntityClassInfo entityClassInfo;
    private Boolean containSubEntity = null;
    public DtoClassInfo(IEntityDtoServiceRelationMap relationMap, Class<?> clazz, EntityClassInfo entityClassInfo) {
        this.clazz = clazz;
        this.relationMap = relationMap;
        this.entityDtoServiceRelation = relationMap.getByDtoClass(this.clazz);
        this.entityClassInfo = entityClassInfo;
        this.dtoFieldList = new ArrayList<>();
        this.normalFieldList = new ArrayList<>();

        Arrays.stream(clazz.getDeclaredFields()).forEach(x -> {
            FieldTypeInfo fieldTypeInfo = FieldTypeInfo.parseField(this.clazz, x);
            EntityDtoServiceRelation relation = relationMap.getByDtoClass((Class)fieldTypeInfo.getActualType());
            if(relation != null) {
                dtoFieldList.add(new DtoField(x.getName(), fieldTypeInfo, this, relation));
            } else {
                normalFieldList.add(fieldTypeInfo);
            }
        });

        this.dtoFieldList.forEach(x -> {
            if (x.getHasEntityClass() == null) {
                EntityDtoServiceRelation relation = relationMap.getByDtoClass((Class) x.getFieldTypeInfo().getActualType());
                FieldTypeInfo fieldTypeInfo = this.entityClassInfo.getFieldByClass(relation.getEntity());
                if(fieldTypeInfo != null && fieldTypeInfo.getIsCollection() == x.getFieldTypeInfo().getIsCollection()) {
                    x.setHasEntityClass(true);
                    x.setRelatedEntityClass((Class) fieldTypeInfo.getActualType());
                } else {
                    x.setHasEntityClass(false);
                }
            }
        });
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<DtoField> getDtoFieldList() {
        return dtoFieldList;
    }

    public List<FieldTypeInfo> getNormalFieldList() {
        return normalFieldList;
    }

    public EntityDtoServiceRelation getEntityDtoServiceRelation() {
        return entityDtoServiceRelation;
    }

    public EntityClassInfo getEntityClassInfo() {
        return entityClassInfo;
    }

    public boolean containSubDto() {
        return this.dtoFieldList.size() > 0;
    }

    public boolean containSubEntity() {
        return this.dtoFieldList.stream().filter(x -> x.getHasEntityClass()).count() > 0;
    }
}
