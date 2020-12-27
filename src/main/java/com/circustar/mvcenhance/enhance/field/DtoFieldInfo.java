package com.circustar.mvcenhance.enhance.field;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.common.query.EntityFilter;
import com.circustar.mvcenhance.enhance.utils.SPELParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public class DtoFieldInfo {
    private EntityDtoServiceRelation relationInfo;
    private String dtoName;
    private Class dtoClass;

    public EntityDtoServiceRelation getRelationInfo() {
        return relationInfo;
    }

    public void setRelationInfo(EntityDtoServiceRelation relationInfo) {
        this.relationInfo = relationInfo;
    }

    public String getDtoName() {
        return dtoName;
    }

    public void setDtoName(String dtoName) {
        this.dtoName = dtoName;
    }

    public Class getDtoClass() {
        return dtoClass;
    }

    public void setDtoClass(Class dtoClass) {
        this.dtoClass = dtoClass;
    }

    public FieldTypeInfo getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(FieldTypeInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    private FieldTypeInfo fieldInfo;
    public DtoFieldInfo(EntityDtoServiceRelation relationInfo, String dtoName, Class dtoClass) {
        this.relationInfo = relationInfo;
        this.dtoName = dtoName;
        this.dtoClass = dtoClass;

        fieldInfo = this.parseField();
//        if(fieldInfo == null) {
//            return;
//        }
    }

    private FieldTypeInfo parseField() {
        FieldTypeInfo f = null;
        if(!StringUtils.isEmpty(this.dtoName)) {
            f = FieldTypeInfo.parseFieldByName(relationInfo.getDto(), this.dtoName);
            //f = PropertyUtils.findFieldByName(entityInfo.getDto(), this.subDtoName);
        }
        if(f != null || this.dtoClass == null) {
            return f;
        }
        return FieldTypeInfo.parseFieldByClass(relationInfo.getDto(), this.getDtoClass(), true);
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


