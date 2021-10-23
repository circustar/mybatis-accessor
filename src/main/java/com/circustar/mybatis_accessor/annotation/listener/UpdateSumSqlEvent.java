package com.circustar.mybatis_accessor.annotation.listener;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;

import java.util.List;

public class UpdateSumSqlEvent extends UpdateCountSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static final String originalSql = "select sum(t1.%s) from %s t1 where t1.%s = %s.%s";

    protected String getOriginalSql(String[] originParams) {
        return UpdateSumSqlEvent.originalSql;
    }

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        List<DtoField> dtoFields = super.parseDtoFieldList(updateEventModel, dtoClassInfo);
        String sPartFieldName = updateEventModel.getUpdateParams()[2];
        DtoField sPartField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sPartFieldName);
        dtoFields.add(sPartField);
        return dtoFields;
    }

    @Override
    protected String CreateSqlPart(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo
            , TableInfo tableInfo, DtoClassInfo subDtoClassInfo
            , TableInfo subTableInfo, List<DtoField> dtoFields) {
        String selectSql = String.format(getOriginalSql(updateEventModel.getUpdateParams())
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
