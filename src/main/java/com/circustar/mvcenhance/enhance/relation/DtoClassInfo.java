package com.circustar.mvcenhance.enhance.relation;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.util.StringUtils;

import java.util.List;

//TODO:CACHE
public class DtoClassInfo {
    public DtoClassInfo(Class dtoClass, Class entityClass) {
        this.dtoClass = dtoClass;
        this.entityClass = entityClass;
        this.entityTableInfo = TableInfoHelper.getTableInfo(entityClass);
        this.fieldInfoList = DtoField.parseDtoFields(dtoClass);
    }
    private Class dtoClass;
    private Class entityClass;
    private TableInfo entityTableInfo;
    private List<DtoField> fieldInfoList;

    public DtoField findDtoField(String fieldName) {
        if(StringUtils.isEmpty(fieldName)) {
            return null;
        }
        for(DtoField f : fieldInfoList) {
            if(f.getFieldName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    public Class getDtoClass() {
        return dtoClass;
    }

    public void setDtoClass(Class dtoClass) {
        this.dtoClass = dtoClass;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public TableInfo getEntityTableInfo() {
        return entityTableInfo;
    }

    public void setEntityTableInfo(TableInfo entityTableInfo) {
        this.entityTableInfo = entityTableInfo;
    }

    public List<DtoField> getFieldInfoList() {
        return fieldInfoList;
    }

    public void setFieldInfoList(List<DtoField> fieldInfoList) {
        this.fieldInfoList = fieldInfoList;
    }


}
