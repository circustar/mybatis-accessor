package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateAssignEvent extends UpdateAvgEvent implements IUpdateEvent<UpdateEventModel> {
    public UpdateAssignEvent(MybatisAccessorService mybatisAccessorService) {
        super(mybatisAccessorService);
    }

    protected boolean patternProvidedWeight = false;

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        List<DtoField> dtoFields = super.parseDtoFieldList(updateEventModel, dtoClassInfo);
        String sWeightFieldName = updateEventModel.getUpdateParams().get(4);
        DtoField sWeightField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sWeightFieldName);
        dtoFields.add(sWeightField);
        if(updateEventModel.getUpdateParams().size() >= 6) {
            String sAllWeightFieldName = updateEventModel.getUpdateParams().get(5);
            DtoField sAllWeightField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sAllWeightFieldName);
            dtoFields.add(sAllWeightField);
            this.patternProvidedWeight = true;
        }
        return dtoFields;
    }

    protected BigDecimal getTotalWeight(List sEntityList, DtoField sWeightEntityField) throws MybatisAccessorException {
        Class sWeightEntityClass = sWeightEntityField.getActualClass();
        return NumberUtils.sumListByType(sWeightEntityClass, sEntityList
                , sWeightEntityField.getPropertyDescriptor().getReadMethod());
    }

    protected BigDecimal getNextWeight(Object sEntity, DtoField sWeightEntityField) throws MybatisAccessorException {
        return NumberUtils.readDecimalValue(sEntity
                , sWeightEntityField.getPropertyDescriptor().getReadMethod());
    }

    protected DtoField getWeightEntityField(List<DtoField> dtoFields) {
        return dtoFields.get(3);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams
            , String updateEventLogId) throws MybatisAccessorException {
        Method mKeyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();

        DtoField mField = dtoFields.get(0);
        Method mFieldReadMethod = mField.getPropertyDescriptor().getReadMethod();
        DtoField sField = dtoFields.get(1);
        Method sFieldReadMethod = sField.getPropertyDescriptor().getReadMethod();
        Method mWeightFileReadMethod = null;
        if(patternProvidedWeight) {
            mWeightFileReadMethod = dtoFields.get(4).getEntityFieldInfo().getPropertyDescriptor().getReadMethod();
        }

        DtoField assignField = dtoFields.get(2);
        Class sAssignFieldType = assignField.getActualClass();
        DtoField sWeightField = this.getWeightEntityField(dtoFields);
        ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();
        int scale = (int)parsedParams.get(0);

        List updateSubDtoList = new ArrayList();
        for(Object dto : dtoList) {
            Serializable mKeyValue = (Serializable) FieldUtils.getFieldValue(dto, mKeyFieldReadMethod);
            Object dtoUpdated = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), mKeyValue
                    , false, Collections.singletonList(sField.getField().getName()));

            BigDecimal mSumValue = NumberUtils.readDecimalValue(dtoUpdated, mFieldReadMethod);
            if(mSumValue == null) {
                continue;
            }
            Object fieldValue = FieldUtils.getFieldValue(dtoUpdated, sFieldReadMethod);
            List sFieldValueList = CollectionUtils.convertToList(fieldValue);
            BigDecimal allWeightValue ;
            if(mWeightFileReadMethod != null) {
                allWeightValue = NumberUtils.readDecimalValue(dto, sFieldReadMethod);
            } else {
                allWeightValue = this.getTotalWeight(sFieldValueList, sWeightField);
            }
            BigDecimal sumAssignValue = BigDecimal.ZERO;
            BigDecimal sumWeightValue = BigDecimal.ZERO;

            BigDecimal nextSumWeightValue;
            BigDecimal nextSumAssignValue;
            for(Object sEntity : sFieldValueList) {
                nextSumWeightValue = sumWeightValue.add(this.getNextWeight(sEntity, sWeightField));
                nextSumAssignValue = mSumValue.multiply(nextSumWeightValue).divide(allWeightValue, scale, RoundingMode.HALF_DOWN);
                BigDecimal assignValue = nextSumAssignValue.subtract(sumAssignValue);
                BigDecimal oldValue = NumberUtils.readDecimalValue(sEntity, assignField.getPropertyDescriptor().getReadMethod());

                if(assignValue.compareTo(oldValue) != 0) {
                    FieldUtils.setFieldValue(sEntity, assignField.getPropertyDescriptor().getWriteMethod()
                            , NumberUtils.castFromBigDecimal(sAssignFieldType, assignValue));
                    updateSubDtoList.add(sEntity);
                }

                sumAssignValue = nextSumAssignValue;
                sumWeightValue = nextSumWeightValue;
            }
        }
        if(!updateSubDtoList.isEmpty()) {
            mybatisAccessorService.updateList(fieldDtoClassInfo.getEntityDtoServiceRelation(), updateSubDtoList
                    , false, null, false, updateEventLogId);
        }
    }
}
