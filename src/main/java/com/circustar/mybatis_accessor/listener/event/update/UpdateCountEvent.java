package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpdateCountEvent extends UpdateCountSqlEvent implements IUpdateEvent<UpdateEventModel> {

    protected MybatisAccessorService mybatisAccessorService;
    public UpdateCountEvent(MybatisAccessorService mybatisAccessorService) {
        this.mybatisAccessorService = mybatisAccessorService;
    }

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields
            , DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        return Arrays.asList(updateEventModel.getUpdateParams());
    }

    protected BigDecimal getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object fieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        int count = 0;
        if(fieldValue != null) {
            count = CollectionUtils.convertToList(fieldValue).size();
        }
        return BigDecimal.valueOf(count);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams
            , String updateEventLogId) {
        ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();
        DtoField mField = dtoFields.get(0);
        DtoField sField = dtoFields.get(1);
        Method mKeyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();

        List updateDtoList = new ArrayList();
        for(Object dto : dtoList) {
            Serializable mKeyValue = (Serializable) FieldUtils.getFieldValue(dto, mKeyFieldReadMethod);
            Object dtoUpdated = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), mKeyValue
                    , false, Collections.singletonList(sField.getField().getName()));
            BigDecimal oldValue = NumberUtils.readDecimalValue(dto, mField.getPropertyDescriptor().getReadMethod());
            BigDecimal newValue = getValue(dtoUpdated, dtoFields, parsedParams);
            if(newValue.compareTo(oldValue) == 0) {
                continue;
            }
            FieldUtils.setFieldValue(dtoUpdated, mField.getPropertyDescriptor().getWriteMethod()
                    , NumberUtils.castFromBigDecimal(mField.getActualClass(), newValue));
            updateDtoList.add(dtoUpdated);
        }

        if(!updateDtoList.isEmpty()) {
            mybatisAccessorService.updateList(dtoClassInfo.getEntityDtoServiceRelation(), updateDtoList
                    , false, null, false, updateEventLogId);
        }
    }
}
