package com.circustar.mybatis_accessor.annotation.after_update_executor;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.util.List;

public class AfterUpdateCountExecutor implements  IAfterUpdateExecutor {
    private static final String selectSql = "select count(*) from %s t1 where t1.%s = %s.%s";

    private static final AfterUpdateCountExecutor instance = new AfterUpdateCountExecutor();

    @Override
    public ExecuteTiming getExecuteTiming() {
        return ExecuteTiming.AFTER_UPDATE;
    }

    @Override
    public void exec(DtoClassInfo dtoClassInfo, List<Object> entityList, String[] params) {
        String resultFieldName = params[0];
        String targetFieldName = params[1];
        DtoField targetField = dtoClassInfo.getDtoField(targetFieldName);
        DtoField resultField = dtoClassInfo.getDtoField(resultFieldName);
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();

        DtoClassInfo fieldDtoClassInfo = resultField.getFieldDtoClassInfo();
        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        String subSql = String.format(selectSql
                , subTableInfo.getTableName()
                , tableInfo.getKeyColumn()
                , tableInfo.getTableName()
                , tableInfo.getKeyColumn());

        if(subTableInfo.getLogicDeleteFieldInfo() != null) {
            subSql = subSql + " and t1." + subTableInfo.getLogicDeleteFieldInfo().getColumn() + " = " + subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
        }
        IService serviceBean = dtoClassInfo.getServiceBean();
        for(int i = 0; i< entityList.size(); i++) {
            EntityFieldInfo keyField = dtoClassInfo.getEntityClassInfo().getKeyField();
            Object keyValue = FieldUtils.getFieldValue(entityList.get(i), keyField.getPropertyDescriptor().getReadMethod());

            UpdateWrapper uw = new UpdateWrapper();
            uw.setSql(targetField.getEntityFieldInfo().getColumnName() + " = (" + subSql + ")");
            uw.eq(keyField.getColumnName(), keyValue);
            serviceBean.update(uw);
        }
    }
}
