package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.lang.reflect.Method;
import java.util.List;
// 将一个值分配到子表的字段上
// 参数1：需要分配的值对应的变量名
// 参数2：子表对应DTO中的变量名
// 参数3：分配依据对应的变量名
// 参数4：分配目标字段对应的变量名
// 参数5（可选）：精度，默认0
public class AfterUpdateAssignExecutor implements  IAfterUpdateExecutor {
    private static final String selectSql = "Round((sum(%s) over (partition by %s order by %s)) / (sum(%s) over (partition by %s)) * %s, %s) " +
            "- Round(((sum(%s) over (partition by %s order by %s)) - %s) / (sum(%s) over (partition by %s)) * %s, %s) as %s";

    private static final AfterUpdateAssignExecutor instance = new AfterUpdateAssignExecutor();

    @Override
    public ExecuteTiming getExecuteTiming() {
        return ExecuteTiming.AFTER_UPDATE;
    }

    @Override
    public void exec(DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params) {
        String summaryFieldName = params[0];
        String resultFieldName = params[1];
        String accordingFieldName = params[2];
        String assignFieldName = params[3];
        String precision = "0";
        if(params.length > 4) {
            precision = params[4];
        }

        DtoField dtoField = dtoClassInfo.getDtoField(resultFieldName);
        DtoClassInfo fieldDtoClassInfo = dtoField.getFieldDtoClassInfo();
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();
        String summaryTableId = tableInfo.getKeyColumn();

        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        String assignTableId = subTableInfo.getKeyColumn();

        Method summaryFieldReadMethod = dtoClassInfo.getDtoField(summaryFieldName)
                .getEntityFieldInfo().getPropertyDescriptor().getReadMethod();

        DtoField assignDtoField = fieldDtoClassInfo.getDtoField(assignFieldName);
        String assignColumnName = assignDtoField.getEntityFieldInfo().getColumnName();

        dtoField = fieldDtoClassInfo.getDtoField(accordingFieldName);
        String accordingColumnName = dtoField.getEntityFieldInfo().getColumnName();

        String assignTemplateSql = String.format(selectSql, accordingColumnName, summaryTableId, assignTableId
                , accordingColumnName, summaryTableId, "%s", precision, accordingColumnName
                , summaryTableId, assignTableId, accordingColumnName, accordingColumnName, summaryTableId
                , "%s", precision, assignColumnName);

        IService serviceBean = fieldDtoClassInfo.getServiceBean();
        EntityFieldInfo mainKeyField = dtoClassInfo.getEntityClassInfo().getKeyField();
        Method keyFieldReadMethod = fieldDtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod();
        Method assignFieldReadMethod = assignDtoField.getEntityFieldInfo().getPropertyDescriptor().getReadMethod();
        for(int i = 0; i< entityList.size(); i++) {
            Object keyValue = FieldUtils.getFieldValue(entityList.get(i), mainKeyField.getPropertyDescriptor().getReadMethod());
            String summaryValue = FieldUtils.getFieldValue(entityList.get(i), summaryFieldReadMethod).toString();

            String assignValueSql = String.format(assignTemplateSql, summaryValue, summaryValue);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.select(assignTableId,summaryTableId,assignValueSql);
            queryWrapper.eq(summaryTableId, keyValue);
            if(fieldDtoClassInfo.getDeleteFlagField() != null) {
                queryWrapper.eq(fieldDtoClassInfo.getDeleteFlagField().getEntityFieldInfo().getColumnName()
                        , subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue());
            }
            List subEntityList = serviceBean.list(queryWrapper);
            if(subEntityList.isEmpty()) {
                throw new RuntimeException("assign target not found");
            }
            for(Object subEntity : subEntityList) {
                UpdateWrapper uw = new UpdateWrapper();
                Object subKeyValue = FieldUtils.getFieldValue(subEntity, keyFieldReadMethod);
                uw.eq(assignTableId, subKeyValue);
                Object assignValue = FieldUtils.getFieldValue(subEntity, assignFieldReadMethod);
                uw.set(assignColumnName, assignValue);
                serviceBean.update(uw);
            }
        }
    }
}
