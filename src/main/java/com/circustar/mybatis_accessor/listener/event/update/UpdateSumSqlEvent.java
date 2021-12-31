package com.circustar.mybatis_accessor.listener.event.update;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;

import java.util.List;

public class UpdateSumSqlEvent extends UpdateCountSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static final String ORIGINAL_SQL = "select sum(t1.%s) from %s t1 where t1.%s = %s.%s";

    protected String getOriginalSql(List<String> originParams) {
        return UpdateSumSqlEvent.ORIGINAL_SQL;
    }

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        List<DtoField> dtoFields = super.parseDtoFieldList(updateEventModel, dtoClassInfo);
        String sPartFieldName = updateEventModel.getUpdateParams().get(2);
        DtoField sPartField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sPartFieldName);
        dtoFields.add(sPartField);
        return dtoFields;
    }

    @Override
    protected String createSqlPart(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo
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
