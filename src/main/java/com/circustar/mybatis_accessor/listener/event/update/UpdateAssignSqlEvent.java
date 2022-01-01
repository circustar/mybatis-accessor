package com.circustar.mybatis_accessor.listener.event.update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.class_info.EntityFieldInfo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
// 解决分配问题
// 参数1：需要分配的值
// 参数2：分配目标子表对应的变量名
// 参数3：分配目标字段对应的变量名
// 参数4：精度
// 参数5：分配权重对应的变量名

public class UpdateAssignSqlEvent extends UpdateAvgSqlEvent implements IUpdateEvent<UpdateEventModel> {
    protected static final String SELECT_SQL = "Round((sum(%s) over (partition by %s order by %s)) / (sum(%s) over (partition by %s)) * %s, %s) " +
            "- Round(((sum(%s) over (partition by %s order by %s)) - %s) / (sum(%s) over (partition by %s)) * %s, %s) as %s";

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        List<DtoField> dtoFields = super.parseDtoFieldList(updateEventModel, dtoClassInfo);
        String sWeightFieldName = updateEventModel.getUpdateParams().get(4);
        DtoField sWeightField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sWeightFieldName);
        dtoFields.add(sWeightField);
        return dtoFields;
    }

    @Override
    protected String createSqlPart(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo, TableInfo tableInfo
            , DtoClassInfo subDtoClassInfo, TableInfo subTableInfo, List<DtoField> dtoFields) {
        String mainTableId = tableInfo.getKeyColumn();
        String sTableId = subTableInfo.getKeyColumn();
        if(tableInfo.equals(subTableInfo)) {
            mainTableId = dtoClassInfo.getEntityClassInfo().getIdReferenceFieldInfo().getColumnName();
        }
        String sAssignColumnName = dtoFields.get(2).getEntityFieldInfo().getColumnName();
        String sWeightColumnName = dtoFields.get(3).getEntityFieldInfo().getColumnName();
        String precision = updateEventModel.getUpdateParams().get(3);

        return String.format(SELECT_SQL
                , sWeightColumnName, mainTableId, sTableId, sWeightColumnName
                , mainTableId, "%s", precision
                , sWeightColumnName, mainTableId, sTableId, sWeightColumnName
                , sWeightColumnName, mainTableId, "%s", precision
                , sAssignColumnName);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams
            , String updateEventLogId) {
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();
        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        Method mKeyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        Method mFieldReadMethod =dtoFields.get(0).getEntityFieldInfo().getPropertyDescriptor().getReadMethod();
        String assignTemplateSql = parsedParams.get(0).toString();
        String mTableId = tableInfo.getKeyColumn();
        if(fieldDtoClassInfo.equals(dtoClassInfo)) {
            mTableId = dtoClassInfo.getEntityClassInfo().getIdReferenceFieldInfo().getColumnName();
        }
        String sTableId = subTableInfo.getKeyColumn();
        Method sKeyFieldReadMethod = fieldDtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod();
        EntityFieldInfo sAssignEntityFieldInfo = dtoFields.get(2).getEntityFieldInfo();
        Method sAssignFieldReadMethod = sAssignEntityFieldInfo.getPropertyDescriptor().getReadMethod();
        String sAssignColumnName = sAssignEntityFieldInfo.getColumnName();

        IService mServiceBean = dtoClassInfo.getServiceBean();
        IService sServiceBean = fieldDtoClassInfo.getServiceBean();

        QueryWrapper queryWrapper = new QueryWrapper();
        UpdateWrapper updateWrapper = new UpdateWrapper();
        for(Object o : dtoList) {
            Object keyValue = FieldUtils.getFieldValue(o, mKeyFieldReadMethod);
            Object entity = mServiceBean.getById((Serializable) keyValue);
            Object summaryValue = FieldUtils.getFieldValue(entity, mFieldReadMethod);
            if(summaryValue == null) {
                continue;
            }

            String assignValueSql = String.format(assignTemplateSql, summaryValue, summaryValue);
            queryWrapper.clear();
            queryWrapper.select(sTableId,mTableId,assignValueSql);
            queryWrapper.eq(mTableId, keyValue);
            if(fieldDtoClassInfo.getDeleteFlagField() != null) {
                queryWrapper.eq(fieldDtoClassInfo.getDeleteFlagField().getEntityFieldInfo().getColumnName()
                        , subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue());
            }
            List subEntityList = sServiceBean.list(queryWrapper);
            for(Object subEntity : subEntityList) {
                updateWrapper.clear();
                Object subKeyValue = FieldUtils.getFieldValue(subEntity, sKeyFieldReadMethod);
                updateWrapper.eq(sTableId, subKeyValue);
                Object assignValue = FieldUtils.getFieldValue(subEntity, sAssignFieldReadMethod);
                updateWrapper.set(sAssignColumnName, assignValue);
                sServiceBean.update(updateWrapper);
            }
        }
    }
}
