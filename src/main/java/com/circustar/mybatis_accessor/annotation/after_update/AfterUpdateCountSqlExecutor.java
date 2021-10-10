package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AfterUpdateCountSqlExecutor implements  IAfterUpdateExecutor {
    private static final String originalSql = "select count(*) from %s t1 where t1.%s = %s.%s";

    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        String mFieldName = params[0];
        String sFieldName = params[1];
        DtoField mField = dtoClassInfo.getDtoField(mFieldName);
        DtoField sField = dtoClassInfo.getDtoField(sFieldName);

        ArrayList<DtoField> dtoFields = new ArrayList<>();
        dtoFields.add(mField);
        dtoFields.add(sField);
        return dtoFields;
    }

    protected List<Object> parseParams(List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, String[] originParams) {
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();
        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        String selectSql = CreateSqlPart(dtoClassInfo, tableInfo, fieldDtoClassInfo, subTableInfo, dtoFields, originParams);
        return Collections.singletonList(selectSql);
    }

    protected String CreateSqlPart(DtoClassInfo dtoClassInfo, TableInfo tableInfo, DtoClassInfo subDtoClassInfo, TableInfo subTableInfo, List<DtoField> dtoFields, String[] originParams) {
        String upperKeyColumn = tableInfo.getKeyColumn();
        if(subTableInfo == tableInfo) {
            upperKeyColumn = dtoFields.get(0).getDtoClassInfo().getEntityClassInfo().getIdReferenceFieldInfo().getColumnName();
        }
        String selectSql = String.format(AfterUpdateCountSqlExecutor.originalSql
                , subTableInfo.getTableName()
                , upperKeyColumn
                , tableInfo.getTableName()
                , tableInfo.getKeyColumn());

        if(subTableInfo.getLogicDeleteFieldInfo() != null) {
            selectSql = selectSql + " and t1." + subTableInfo.getLogicDeleteFieldInfo().getColumn() + " = " + subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
        }
        return selectSql;
    }

    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        DtoField mField = dtoFields.get(0);
        IService serviceBean = dtoClassInfo.getServiceBean();
        String execSelectSql = parsedParams.get(0).toString();
        for(int i = 0; i< entityList.size(); i++) {
            EntityFieldInfo keyField = dtoClassInfo.getEntityClassInfo().getKeyField();
            Object keyValue = FieldUtils.getFieldValue(entityList.get(i), keyField.getPropertyDescriptor().getReadMethod());

            UpdateWrapper uw = new UpdateWrapper();
            uw.setSql(mField.getEntityFieldInfo().getColumnName() + " = (" + execSelectSql + ")");
            uw.eq(keyField.getColumnName(), keyValue);
            serviceBean.update(uw);
        }
    }

    @Override
    public void exec(DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params) {
        List<DtoField> dtoFields = parseDtoFieldList(dtoClassInfo, params);
        DtoClassInfo fieldDtoClassInfo = dtoFields.get(1).getFieldDtoClassInfo();
        List<Object> parsedParams = parseParams(dtoFields, dtoClassInfo, fieldDtoClassInfo, params);
        execUpdate(dtoClassInfo, fieldDtoClassInfo, entityList, dtoFields, parsedParams);
    }
}
