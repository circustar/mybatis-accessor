package com.circustar.mybatis_accessor.annotation.listener.assign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.listener.IUpdateEvent;
import com.circustar.mybatis_accessor.annotation.listener.summary.UpdateAvgSqlEvent;
import com.circustar.mybatis_accessor.annotation.listener.UpdateEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

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
    protected static final String selectSql = "Round((sum(%s) over (partition by %s order by %s)) / (sum(%s) over (partition by %s)) * %s, %s) " +
            "- Round(((sum(%s) over (partition by %s order by %s)) - %s) / (sum(%s) over (partition by %s)) * %s, %s) as %s";

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        List<DtoField> dtoFields = super.parseDtoFieldList(updateEventModel, dtoClassInfo);
        String sWeightFieldName = updateEventModel.getUpdateParams()[4];
        DtoField sWeightField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sWeightFieldName);
        dtoFields.add(sWeightField);
        return dtoFields;
    }

    @Override
    protected String CreateSqlPart(UpdateEventModel updateEventModel,DtoClassInfo dtoClassInfo, TableInfo tableInfo
            , DtoClassInfo subDtoClassInfo, TableInfo subTableInfo, List<DtoField> dtoFields) {
        String mTableId = tableInfo.getKeyColumn();
        String sTableId = subTableInfo.getKeyColumn();
        if(tableInfo == subTableInfo) {
            mTableId = dtoClassInfo.getEntityClassInfo().getIdReferenceFieldInfo().getColumnName();
        }
        String sAssignColumnName = dtoFields.get(2).getEntityFieldInfo().getColumnName();
        String sWeightColumnName = dtoFields.get(3).getEntityFieldInfo().getColumnName();
        String precision = updateEventModel.getUpdateParams()[3];

        return String.format(selectSql
                , sWeightColumnName, mTableId, sTableId, sWeightColumnName
                , mTableId, "%s", precision
                , sWeightColumnName, mTableId, sTableId, sWeightColumnName
                , sWeightColumnName, mTableId, "%s", precision
                , sAssignColumnName);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();
        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        Method mKeyFieldReadMethod = dtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod();
        Method mFieldReadMethod =dtoFields.get(0).getEntityFieldInfo().getPropertyDescriptor().getReadMethod();
        String assignTemplateSql = parsedParams.get(0).toString();
        String mTableId = tableInfo.getKeyColumn();
        if(fieldDtoClassInfo == dtoClassInfo) {
            mTableId = dtoClassInfo.getEntityClassInfo().getIdReferenceFieldInfo().getColumnName();
        }
        String sTableId = subTableInfo.getKeyColumn();
        Method sKeyFieldReadMethod = fieldDtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod();
        EntityFieldInfo sAssignEntityFieldInfo = dtoFields.get(2).getEntityFieldInfo();
        Method sAssignFieldReadMethod = sAssignEntityFieldInfo.getPropertyDescriptor().getReadMethod();
        String sAssignColumnName = sAssignEntityFieldInfo.getColumnName();

        IService mServiceBean = dtoClassInfo.getServiceBean();
        IService sServiceBean = fieldDtoClassInfo.getServiceBean();

        for(Object o : entityList) {
            Object keyValue = FieldUtils.getFieldValue(o, mKeyFieldReadMethod);
            Object entity = mServiceBean.getById((Serializable) keyValue);
            Object summaryValue = FieldUtils.getFieldValue(entity, mFieldReadMethod);
            if(summaryValue == null) {
                continue;
            }

            String assignValueSql = String.format(assignTemplateSql, summaryValue, summaryValue);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.select(sTableId,mTableId,assignValueSql);
            queryWrapper.eq(mTableId, keyValue);
            if(fieldDtoClassInfo.getDeleteFlagField() != null) {
                queryWrapper.eq(fieldDtoClassInfo.getDeleteFlagField().getEntityFieldInfo().getColumnName()
                        , subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue());
            }
            List subEntityList = sServiceBean.list(queryWrapper);
            for(Object subEntity : subEntityList) {
                UpdateWrapper uw = new UpdateWrapper();
                Object subKeyValue = FieldUtils.getFieldValue(subEntity, sKeyFieldReadMethod);
                uw.eq(sTableId, subKeyValue);
                Object assignValue = FieldUtils.getFieldValue(subEntity, sAssignFieldReadMethod);
                uw.set(sAssignColumnName, assignValue);
                sServiceBean.update(uw);
            }
        }
    }
}
