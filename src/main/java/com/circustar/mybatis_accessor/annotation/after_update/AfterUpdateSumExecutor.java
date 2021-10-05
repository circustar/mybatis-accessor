package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;

import java.util.Arrays;
import java.util.List;

public class AfterUpdateSumExecutor extends AfterUpdateCountExecutor implements  IAfterUpdateExecutor {
    private static final String originalSql = "select sum(t1.%s) from %s t1 where t1.%s = %s.%s";

    protected String getOriginalSql() {
        return AfterUpdateSumExecutor.originalSql;
    }

    @Override
    public ExecuteTiming getExecuteTiming() {
        return ExecuteTiming.AFTER_UPDATE;
    }

    @Override
    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        String resultFieldName = params[0];
        String targetFieldName = params[1];
        String subFieldName = params[2];
        DtoField resultField = dtoClassInfo.getDtoField(resultFieldName);
        DtoField targetField = dtoClassInfo.getDtoField(targetFieldName);
        DtoField subFieldInfo = targetField.getFieldDtoClassInfo().getDtoField(subFieldName);

        return Arrays.asList(resultField, targetField, subFieldInfo);

    }

    @Override
    protected String CreateSqlPart(DtoClassInfo dtoClassInfo, TableInfo tableInfo, TableInfo subTableInfo, List<DtoField> dtoFields) {
        String selectSql = String.format(getOriginalSql()
                , dtoFields.get(2).getEntityFieldInfo().getColumnName()
                , subTableInfo.getTableName()
                , tableInfo.getKeyColumn()
                , tableInfo.getTableName()
                , tableInfo.getKeyColumn());

        if(subTableInfo.getLogicDeleteFieldInfo() != null) {
            selectSql = selectSql + " and t1." + subTableInfo.getLogicDeleteFieldInfo().getColumn() + " = " + subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
        }
        return selectSql;
    }
}
