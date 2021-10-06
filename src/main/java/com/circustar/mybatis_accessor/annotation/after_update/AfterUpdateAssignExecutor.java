package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
import com.sun.org.apache.bcel.internal.generic.BIPUSH;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AfterUpdateAssignExecutor extends AfterUpdateAvgExecutor implements  IAfterUpdateExecutor {
    @Override
    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        List<DtoField> dtoFields = super.parseDtoFieldList(dtoClassInfo, params);
        String sWeightFieldName = params[4];
        DtoField sWeightField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sWeightFieldName);
        dtoFields.add(sWeightField);
        return dtoFields;
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        EntityFieldInfo mKeyField = dtoClassInfo.getEntityClassInfo().getKeyField();
        Method mKeyFieldReadMethod = mKeyField.getPropertyDescriptor().getReadMethod();
        EntityFieldInfo mEnittyField = dtoFields.get(0).getEntityFieldInfo();
        Method mFieldReadMethod = mEnittyField.getPropertyDescriptor().getReadMethod();

        TableInfo sTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        TableFieldInfo sTableLogicDeleteFieldInfo = sTableInfo.getLogicDeleteFieldInfo();

        EntityFieldInfo sKeyField = fieldDtoClassInfo.getKeyField().getEntityFieldInfo();
        Method sKeyFieldReadMethod = sKeyField.getPropertyDescriptor().getReadMethod();

        EntityFieldInfo sWeightEntityField = dtoFields.get(3).getEntityFieldInfo();
        Class sWeightEntityClass = (Class) sWeightEntityField.getActualType();
        Method sWeightFieldReadMethod = sWeightEntityField.getPropertyDescriptor().getReadMethod();

        Class sAssignFieldType = dtoFields.get(2).getEntityFieldInfo().getField().getType();
        String sAssignColumnName = dtoFields.get(2).getEntityFieldInfo().getColumnName();

        IService sServiceBean = fieldDtoClassInfo.getServiceBean();
        int scale = (int)parsedParams.get(0);

        for(Object entity : entityList) {
            Object mKeyValue = FieldUtils.getFieldValue(entity, mKeyFieldReadMethod);
            BigDecimal mSumValue = NumberUtils.readDecimalValue((Class) mEnittyField.getActualType(), entity, mFieldReadMethod);

            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq(mKeyField.getColumnName(), mKeyValue);

            if(sTableLogicDeleteFieldInfo!= null) {
                queryWrapper.eq(sTableLogicDeleteFieldInfo.getColumn(), sTableLogicDeleteFieldInfo.getLogicNotDeleteValue());
            }
            List sEntityList = sServiceBean.list(queryWrapper);
            BigDecimal allWeightValue = NumberUtils.sumListByType(sWeightEntityClass, sEntityList, sWeightFieldReadMethod);
            BigDecimal sumAssignValue = BigDecimal.ZERO;
            BigDecimal sumWeightValue = BigDecimal.ZERO;

            BigDecimal nextSumWeightValue;
            BigDecimal nextSumAssignValue;
            for(Object sEntity : sEntityList) {
                Object sKeyValue = FieldUtils.getFieldValue(sEntity, sKeyFieldReadMethod);
                nextSumWeightValue = sumWeightValue.add(NumberUtils.readDecimalValue(sWeightEntityClass, sEntity, sWeightFieldReadMethod));
                nextSumAssignValue = mSumValue.multiply(nextSumWeightValue).divide(allWeightValue, scale, RoundingMode.HALF_DOWN);
                Object assignValue = NumberUtils.castFromBigDecimal(sAssignFieldType, nextSumAssignValue.subtract(sumAssignValue));

                UpdateWrapper uw = new UpdateWrapper();
                uw.eq(sKeyField.getColumnName(), sKeyValue);
                uw.set(sAssignColumnName, assignValue);
                sServiceBean.update(uw);

                sumAssignValue = nextSumAssignValue;
                sumWeightValue = nextSumWeightValue;
            }
        }
    }
}
