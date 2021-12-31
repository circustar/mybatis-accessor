package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

public class UpdateSumEvent extends UpdateCountEvent implements IUpdateEvent<UpdateEventModel> {

    public UpdateSumEvent(MybatisAccessorService mybatisAccessorService) {
        super(mybatisAccessorService);
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
    protected BigDecimal getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object subFieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        List valueList = CollectionUtils.convertToList(subFieldValue);
        DtoField sumField = dtoFields.get(2);
        Class type = sumField.getActualClass();
        Method readMethod = sumField.getPropertyDescriptor().getReadMethod();
        return NumberUtils.sumListByType(type, valueList, readMethod);
    }
}
