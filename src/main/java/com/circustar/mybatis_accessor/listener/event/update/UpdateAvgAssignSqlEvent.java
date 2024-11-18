package com.circustar.mybatis_accessor.listener.event.update;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import java.util.List;
public class UpdateAvgAssignSqlEvent extends UpdateAssignSqlEvent implements IUpdateEvent<UpdateEventModel> {
    @Override
    protected String createSqlPart(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo, TableInfo tableInfo, DtoClassInfo subDtoClassInfo
            , TableInfo subTableInfo, List<DtoField> dtoFields) {
        String mainTableId = tableInfo.getKeyColumn();
        String sTableId = subTableInfo.getKeyColumn();
        String sAssignColumnName = dtoFields.get(2).getEntityFieldInfo().getColumnName();
        String sWeightColumnName = "1";
        String precision = updateEventModel.getUpdateParams().get(3);

        return String.format(SELECT_SQL.replace(WEIGHT_VALUE_COLUMN, CALC_WEIGHT_SQL)
                , sWeightColumnName, mainTableId, sTableId, sWeightColumnName
                , mainTableId, "%s", precision
                , sWeightColumnName, mainTableId, sTableId, sWeightColumnName
                , sWeightColumnName, mainTableId, "%s", precision
                , sAssignColumnName);
    }
}
