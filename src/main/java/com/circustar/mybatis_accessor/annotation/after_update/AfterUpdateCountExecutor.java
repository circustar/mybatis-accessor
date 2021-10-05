package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.util.Arrays;
import java.util.List;

public class AfterUpdateCountExecutor implements  IAfterUpdateExecutor {
    private static final String originalSql = "select count(*) from %s t1 where t1.%s = %s.%s";

    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        String resultFieldName = params[0];
        String targetFieldName = params[1];
        DtoField targetField = dtoClassInfo.getDtoField(targetFieldName);
        DtoField resultField = dtoClassInfo.getDtoField(resultFieldName);
        return Arrays.asList(resultField, targetField);
    }

    protected String CreateSqlPart(DtoClassInfo dtoClassInfo, TableInfo tableInfo, TableInfo subTableInfo, List<DtoField> dtoFields) {
        String selectSql = String.format(AfterUpdateCountExecutor.originalSql
                , subTableInfo.getTableName()
                , tableInfo.getKeyColumn()
                , tableInfo.getTableName()
                , tableInfo.getKeyColumn());

        if(subTableInfo.getLogicDeleteFieldInfo() != null) {
            selectSql = selectSql + " and t1." + subTableInfo.getLogicDeleteFieldInfo().getColumn() + " = " + subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
        }
        return selectSql;
    }

    protected void execSql(DtoClassInfo dtoClassInfo, List<Object> entityList, DtoField resultField, String selectSql) {
        IService serviceBean = dtoClassInfo.getServiceBean();
        for(int i = 0; i< entityList.size(); i++) {
            EntityFieldInfo keyField = dtoClassInfo.getEntityClassInfo().getKeyField();
            Object keyValue = FieldUtils.getFieldValue(entityList.get(i), keyField.getPropertyDescriptor().getReadMethod());

            UpdateWrapper uw = new UpdateWrapper();
            uw.setSql(resultField.getEntityFieldInfo().getColumnName() + " = (" + selectSql + ")");
            uw.eq(keyField.getColumnName(), keyValue);
            serviceBean.update(uw);
        }
    }

    @Override
    public ExecuteTiming getExecuteTiming() {
        return ExecuteTiming.AFTER_UPDATE;
    }

    @Override
    public void exec(DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params) {
        List<DtoField> dtoFields = parseDtoFieldList(dtoClassInfo, params);
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();

        DtoClassInfo fieldDtoClassInfo = dtoFields.get(1).getFieldDtoClassInfo();
        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();

        String selectSql = CreateSqlPart(dtoClassInfo, tableInfo, subTableInfo, dtoFields);
        execSql(dtoClassInfo, entityList, dtoFields.get(0), selectSql);
    }
}
