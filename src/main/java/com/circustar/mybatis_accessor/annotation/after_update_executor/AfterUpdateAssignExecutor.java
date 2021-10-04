package com.circustar.mybatis_accessor.annotation.after_update_executor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.util.List;

public class AfterUpdateAssignExecutor implements  IAfterUpdateExecutor {
    private static final String selectSql = "Round((sum(%s) over (partition by %s order by %s) / (sum(t1.%s) over (partition by %s) * %s), %s) " +
            "- Round(((sum(%s) over (partition by %s order by %s) - %s) / (sum(%s) over (partition by %s) * %s), %s) as %s";

    private static final AfterUpdateAssignExecutor instance = new AfterUpdateAssignExecutor();

    @Override
    public ExecuteTiming getExecuteTiming() {
        return ExecuteTiming.AFTER_UPDATE;
    }

    @Override
    public void exec(DtoClassInfo dtoClassInfo
            , List<Object> entityList, String[] params) {
        String summaryFieldName = params[0];
        String resultFieldName = params[1];
        String assignFieldName = params[2];
        String accordingFieldName = params[3];
        String precision = "0";
        if(params.length > 4) {
            precision = params[4];
        }
        String orderFieldName = "";
        if(params.length > 5) {
            orderFieldName = params[5];
        }

        DtoField dtoField = dtoClassInfo.getDtoField(resultFieldName);
        DtoClassInfo fieldDtoClassInfo = dtoField.getFieldDtoClassInfo();
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();
        String summaryTableId = tableInfo.getKeyColumn();

        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        String assignTableId = subTableInfo.getKeyColumn();

        DtoField summaryDtoField = dtoClassInfo.getDtoField(summaryFieldName);

        String assignColumnName = assignFieldName;
        DtoField assignDtoField = fieldDtoClassInfo.getDtoField(assignFieldName);
        if(assignDtoField != null) {
            assignColumnName = assignDtoField.getEntityFieldInfo().getColumnName();
        }

        String accordingColumnName = accordingFieldName;
        dtoField = fieldDtoClassInfo.getDtoField(accordingFieldName);
        if(dtoField != null) {
            accordingColumnName = dtoField.getEntityFieldInfo().getColumnName();
        }

        String orderColumnName;
        dtoField = fieldDtoClassInfo.getDtoField(orderFieldName);
        if(dtoField != null) {
            orderColumnName = dtoField.getEntityFieldInfo().getColumnName();
        } else {
            orderColumnName = assignTableId;
        }

        String assignTemplateSql = String.format(selectSql
                , accordingColumnName
                , summaryTableId
                , orderColumnName
                , accordingColumnName
                , summaryTableId
                , "%s"
                , precision
                , accordingColumnName
                , summaryTableId
                , orderColumnName
                , accordingColumnName
                , summaryTableId
                , "%s"
                , precision
                , assignColumnName);

        IService serviceBean = fieldDtoClassInfo.getServiceBean();
        for(int i = 0; i< entityList.size(); i++) {
            EntityFieldInfo keyField = dtoClassInfo.getEntityClassInfo().getKeyField();
            Object keyValue = FieldUtils.getFieldValue(entityList.get(i), keyField.getPropertyDescriptor().getReadMethod());
            Object summaryValue = FieldUtils.getFieldValue(entityList.get(i), summaryDtoField.getEntityFieldInfo().getPropertyDescriptor().getReadMethod());

            String assignValueSql = String.format(assignTemplateSql, summaryValue.toString(), summaryValue.toString());
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.select(assignTableId,summaryTableId,assignValueSql);
            queryWrapper.eq(summaryTableId, keyValue);
            if(fieldDtoClassInfo.getDeleteFlagField() != null) {
                queryWrapper.eq(fieldDtoClassInfo.getDeleteFlagField().getEntityFieldInfo().getColumnName()
                        , subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue());
            }
            List subEntitylist = serviceBean.list(queryWrapper);
            for(Object subEntity : subEntitylist) {
                UpdateWrapper uw = new UpdateWrapper();
                Object subKeyValue = FieldUtils.getFieldValue(subEntity, fieldDtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
                uw.eq(assignTableId, subKeyValue);
                Object assignValue = FieldUtils.getFieldValue(subEntity, assignDtoField.getPropertyDescriptor().getReadMethod());
                uw.set(assignColumnName, assignValue);
                serviceBean.update(uw);
            }
        }
    }
}
