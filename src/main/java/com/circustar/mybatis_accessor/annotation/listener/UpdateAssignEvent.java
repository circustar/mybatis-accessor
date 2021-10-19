package com.circustar.mybatis_accessor.annotation.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class UpdateAssignEvent extends UpdateAvgEvent implements IUpdateEvent {
    @Override
    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        List<DtoField> dtoFields = super.parseDtoFieldList(dtoClassInfo, params);
        String sWeightFieldName = params[4];
        DtoField sWeightField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sWeightFieldName);
        dtoFields.add(sWeightField);
        return dtoFields;
    }

    protected BigDecimal getTotalWeight(List sEntityList, EntityFieldInfo sWeightEntityField) {
        Class sWeightEntityClass = (Class) sWeightEntityField.getActualType();
        BigDecimal allWeightValue = NumberUtils.sumListByType(sWeightEntityClass, sEntityList
                , sWeightEntityField.getPropertyDescriptor().getReadMethod());
        return allWeightValue;
    }

    protected BigDecimal getNextWeight(Object sEntity, EntityFieldInfo sWeightEntityField) {
        Class sWeightEntityClass = (Class) sWeightEntityField.getActualType();
        BigDecimal bigDecimal = NumberUtils.readDecimalValue(sWeightEntityClass, sEntity
                , sWeightEntityField.getPropertyDescriptor().getReadMethod());
        return bigDecimal;
    }

    protected EntityFieldInfo getWeightEntityField(List<DtoField> dtoFields) {
        return dtoFields.get(3).getEntityFieldInfo();
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        EntityFieldInfo mKeyField = dtoClassInfo.getEntityClassInfo().getKeyField();
        Method mKeyFieldReadMethod = mKeyField.getPropertyDescriptor().getReadMethod();

        EntityFieldInfo mEntityField = dtoFields.get(0).getEntityFieldInfo();
        Method mFieldReadMethod = mEntityField.getPropertyDescriptor().getReadMethod();

        TableInfo sTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();

        EntityFieldInfo sKeyField = fieldDtoClassInfo.getKeyField().getEntityFieldInfo();
        Method sKeyFieldReadMethod = sKeyField.getPropertyDescriptor().getReadMethod();

        EntityFieldInfo sWeightEntityField = this.getWeightEntityField(dtoFields);

        EntityFieldInfo sAssignEntityFieldInfo = dtoFields.get(2).getEntityFieldInfo();
        Class sAssignFieldType = sAssignEntityFieldInfo.getField().getType();
        String sAssignColumnName = sAssignEntityFieldInfo.getColumnName();

        IService mServiceBean = dtoClassInfo.getServiceBean();
        IService sServiceBean = fieldDtoClassInfo.getServiceBean();
        int scale = (int)parsedParams.get(0);

        EntityFieldInfo upperKeyField = mKeyField;
        if(fieldDtoClassInfo.getEntityClassInfo() == dtoClassInfo.getEntityClassInfo()) {
            upperKeyField = dtoClassInfo.getEntityClassInfo().getIdReferenceFieldInfo();
        }

        for(Object o : entityList) {
            Object mKeyValue = FieldUtils.getFieldValue(o, mKeyFieldReadMethod);
            Object entity = mServiceBean.getById((Serializable) mKeyValue);
            BigDecimal mSumValue = NumberUtils.readDecimalValue((Class) mEntityField.getActualType(), entity, mFieldReadMethod);
            if(mSumValue == null) {
                continue;
            }

            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq(upperKeyField.getColumnName(), mKeyValue);

            List sEntityList = sServiceBean.list(queryWrapper);
            BigDecimal allWeightValue = this.getTotalWeight(sEntityList, sWeightEntityField);
            BigDecimal sumAssignValue = BigDecimal.ZERO;
            BigDecimal sumWeightValue = BigDecimal.ZERO;

            BigDecimal nextSumWeightValue;
            BigDecimal nextSumAssignValue;
            for(Object sEntity : sEntityList) {
                Object sKeyValue = FieldUtils.getFieldValue(sEntity, sKeyFieldReadMethod);
                nextSumWeightValue = sumWeightValue.add(this.getNextWeight(sEntity, sWeightEntityField));
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
