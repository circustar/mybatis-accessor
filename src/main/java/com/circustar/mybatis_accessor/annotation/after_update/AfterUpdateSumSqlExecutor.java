package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;

import java.util.List;

public class AfterUpdateSumSqlExecutor extends AfterUpdateCountSqlExecutor implements  IAfterUpdateExecutor {
    private static final String originalSql = "select sum(t1.%s) from %s t1 where t1.%s = %s.%s";

    protected String getOriginalSql(String[] originParams) {
        return AfterUpdateSumSqlExecutor.originalSql;
    }

    @Override
    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        List<DtoField> dtoFields = super.parseDtoFieldList(dtoClassInfo, params);
        String sPartFieldName = params[2];
        DtoField sPartField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sPartFieldName);
        dtoFields.add(sPartField);
        return dtoFields;
    }

    @Override
    protected String CreateSqlPart(TableInfo tableInfo, TableInfo subTableInfo, List<DtoField> dtoFields, String[] originParams) {
        String selectSql = String.format(getOriginalSql(originParams)
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
