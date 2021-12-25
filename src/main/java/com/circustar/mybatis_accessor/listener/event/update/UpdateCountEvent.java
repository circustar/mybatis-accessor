package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
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
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams) {
        ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();
        DtoField mField = dtoFields.get(0);
        DtoField sField = dtoFields.get(1);
        EntityFieldInfo mKeyField = dtoClassInfo.getEntityClassInfo().getKeyField();
        Method mKeyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();

        List updateSubDtoList = new ArrayList();
        for(int i = 0; i< dtoList.size(); i++) {
            Serializable mKeyValue = (Serializable) FieldUtils.getFieldValue(dtoList.get(i), mKeyFieldReadMethod);
            Object dtoUpdated = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), mKeyValue
                    , false, Collections.singletonList(sField.getField().getName()));
            Object count = NumberUtils.castFromBigDecimal(mField.getActualClass()
                    , getValue(dtoUpdated, dtoFields, parsedParams)) ;

            FieldUtils.setFieldValue(dtoUpdated, mField.getPropertyDescriptor().getWriteMethod()
                    , count);
            updateSubDtoList.add(dtoUpdated);
        }

        if(!updateSubDtoList.isEmpty()) {
            mybatisAccessorService.updateList(dtoClassInfo.getEntityDtoServiceRelation(), updateSubDtoList
                    , false, null, false);
        }
    }
}
