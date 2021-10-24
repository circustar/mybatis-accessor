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
    protected String CreateSqlPart(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo, TableInfo tableInfo, DtoClassInfo subDtoClassInfo
            , TableInfo subTableInfo, List<DtoField> dtoFields) {
        String mTableId = tableInfo.getKeyColumn();
        String sTableId = subTableInfo.getKeyColumn();
        String sAssignColumnName = dtoFields.get(2).getEntityFieldInfo().getColumnName();
        String sWeightColumnName = "1";
        String precision = updateEventModel.getUpdateParams()[3];

        return String.format(selectSql
                , sWeightColumnName, mTableId, sTableId, sWeightColumnName
                , mTableId, "%s", precision
                , sWeightColumnName, mTableId, sTableId, sWeightColumnName
                , sWeightColumnName, mTableId, "%s", precision
                , sAssignColumnName);
    }
}
