package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

public class UpdateAnyEvent extends UpdateCountSqlEvent implements IUpdateEvent<UpdateEventModel> {

    protected MybatisAccessorService mybatisAccessorService;
    public UpdateAnyEvent(MybatisAccessorService mybatisAccessorService) {
        super();
        this.mybatisAccessorService = mybatisAccessorService;
    }

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        List<DtoField> dtoFields = super.parseDtoFieldList(updateEventModel, dtoClassInfo);
        String sPartFieldName = updateEventModel.getUpdateParams().get(2);
        DtoField sPartField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sPartFieldName);
        dtoFields.add(sPartField);
        return dtoFields;
    }

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields
            , DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        return Arrays.asList(updateEventModel.getUpdateParams());
    }

    protected Comparable getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object subFieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        Optional var0 = CollectionUtils.convertToList(subFieldValue).stream()
                .filter(x -> x != null).findAny();
        if(!var0.isPresent()) {
            return null;
        }
        DtoField targetField = dtoFields.get(2);
        Method readMethod = targetField.getPropertyDescriptor().getReadMethod();
        return (Comparable) FieldUtils.getFieldValue(var0.get(), readMethod);
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
            Comparable oldValue = (Comparable)FieldUtils.getFieldValue(dto, mField.getPropertyDescriptor().getReadMethod());
            Comparable newValue = getValue(dtoUpdated, dtoFields, parsedParams);
            if(newValue == null || (oldValue != null && newValue.compareTo(oldValue) == 0)) {
                continue;
            }
            FieldUtils.setFieldValue(dtoUpdated, mField.getPropertyDescriptor().getWriteMethod()
                    , newValue);
            updateDtoList.add(dtoUpdated);
        }

        if(!updateDtoList.isEmpty()) {
            mybatisAccessorService.updateList(dtoClassInfo.getEntityDtoServiceRelation(), updateDtoList
                    , false, null, false, updateEventLogId);
        }
    }
}
