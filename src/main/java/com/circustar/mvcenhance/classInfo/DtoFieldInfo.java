//package com.circustar.mvcenhance.classInfo;
//
//import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
//import org.springframework.util.StringUtils;
//
//import java.util.*;
//
//public class DtoFieldInfo {
//    private EntityDtoServiceRelation relationInfo;
//    private String dtoName;
//    private Class dtoClass;
//
//    public EntityDtoServiceRelation getRelationInfo() {
//        return relationInfo;
//    }
//
//    public void setRelationInfo(EntityDtoServiceRelation relationInfo) {
//        this.relationInfo = relationInfo;
//    }
//
//    public String getDtoName() {
//        return dtoName;
//    }
//
//    public void setDtoName(String dtoName) {
//        this.dtoName = dtoName;
//    }
//
//    public Class getDtoClass() {
//        return dtoClass;
//    }
//
//    public void setDtoClass(Class dtoClass) {
//        this.dtoClass = dtoClass;
//    }
//
//    public FieldTypeInfo getFieldInfo() {
//        return fieldInfo;
//    }
//
//    public void setFieldInfo(FieldTypeInfo fieldInfo) {
//        this.fieldInfo = fieldInfo;
//    }
//
//    private FieldTypeInfo fieldInfo;
//    public DtoFieldInfo(EntityDtoServiceRelation relationInfo, String dtoName, Class dtoClass) {
//        this.relationInfo = relationInfo;
//        this.dtoName = dtoName;
//        this.dtoClass = dtoClass;
//
//        fieldInfo = this.parseField();
//    }
//
//    private FieldTypeInfo parseField() {
//        FieldTypeInfo f = null;
//        if(!StringUtils.isEmpty(this.dtoName)) {
//            f = FieldTypeInfo.parseFieldByName(relationInfo.getDtoClass(), this.dtoName);
//        }
//        if(f != null || this.dtoClass == null) {
//            return f;
//        }
//        return FieldTypeInfo.parseFieldByClass(relationInfo.getDtoClass(), this.getDtoClass(), true);
//    }
//
//
//}
//
//
