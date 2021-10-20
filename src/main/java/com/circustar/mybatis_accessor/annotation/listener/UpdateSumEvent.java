package com.circustar.mybatis_accessor.annotation.listener;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

public class UpdateSumEvent extends UpdateCountEvent implements IUpdateEvent {

    @Override
    protected List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params) {
        List<DtoField> dtoFields = super.parseDtoFieldList(dtoClassInfo, params);
        String sPartFieldName = params[2];
        DtoField sPartField = dtoFields.get(1).getFieldDtoClassInfo().getDtoField(sPartFieldName);
        dtoFields.add(sPartField);
        return dtoFields;
    }

    @Override
    protected BigDecimal getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object subFieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        List valueList = CollectionUtils.convertToList(subFieldValue);
        DtoField sumField = dtoFields.get(2);
        Class type = sumField.getActualClass();
        Method readMethod = sumField.getPropertyDescriptor().getReadMethod();
        return NumberUtils.sumListByType(type, valueList, readMethod);
    }
}
