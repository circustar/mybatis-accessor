package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AfterUpdateCountExecutor extends AfterUpdateCountSqlExecutor implements  IAfterUpdateExecutor {

    @Override
    protected List<Object> parseParams(List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, String[] params) {
        return null;
    }

    protected BigDecimal getValue(QueryWrapper queryWrapper, IService fieldServiceBean, List<DtoField> dtoFields, List<Object> parsedParams) {
        return BigDecimal.valueOf(fieldServiceBean.count(queryWrapper));
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        IService serviceBean = dtoClassInfo.getServiceBean();
        IService fieldServiceBean = fieldDtoClassInfo.getServiceBean();
        TableInfo fieldTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        TableFieldInfo fieldLogicDeleteFieldInfo = fieldTableInfo.getLogicDeleteFieldInfo();
        DtoField mField = dtoFields.get(0);
        EntityFieldInfo mKeyField = dtoClassInfo.getEntityClassInfo().getKeyField();
        Method mKeyFieldReadMethod = mKeyField.getPropertyDescriptor().getReadMethod();

        for(int i = 0; i< entityList.size(); i++) {
            Object mKeyValue = FieldUtils.getFieldValue(entityList.get(i), mKeyFieldReadMethod);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq(mKeyField.getColumnName(), mKeyValue);

            if(fieldLogicDeleteFieldInfo!= null) {
                queryWrapper.eq(fieldLogicDeleteFieldInfo.getColumn(), fieldLogicDeleteFieldInfo.getLogicNotDeleteValue());
            }

            BigDecimal finalValue = getValue(queryWrapper, fieldServiceBean, dtoFields, parsedParams);

            UpdateWrapper uw = new UpdateWrapper();
            uw.set(mField.getEntityFieldInfo().getColumnName(), finalValue.intValue());
            uw.eq(mKeyField.getColumnName(), mKeyValue);
            serviceBean.update(uw);
        }
    }
}
