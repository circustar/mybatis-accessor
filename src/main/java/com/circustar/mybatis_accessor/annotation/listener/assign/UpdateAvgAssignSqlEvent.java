package com.circustar.mybatis_accessor.annotation.listener.assign;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.annotation.listener.IUpdateEvent;
import com.circustar.mybatis_accessor.annotation.listener.UpdateEventModel;
import com.circustar.mybatis_accessor.annotation.listener.assign.UpdateAssignSqlEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
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

        return String.format(selectSql
                , sWeightColumnName, mainTableId, sTableId, sWeightColumnName
                , mainTableId, "%s", precision
                , sWeightColumnName, mainTableId, sTableId, sWeightColumnName
                , sWeightColumnName, mainTableId, "%s", precision
                , sAssignColumnName);
    }
}
