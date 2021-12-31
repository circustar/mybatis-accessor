package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UpdateMaxEvent extends UpdateSumEvent implements IUpdateEvent<UpdateEventModel> {
    public UpdateMaxEvent(MybatisAccessorService mybatisAccessorService) {
        super(mybatisAccessorService);
    }

    @Override
    protected BigDecimal getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object subFieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        List valueList = CollectionUtils.convertToList(subFieldValue);
        DtoField updateField = dtoFields.get(2);
        Method readMethod = updateField.getPropertyDescriptor().getReadMethod();
        Optional<BigDecimal> maxValue = valueList.stream().map(x -> NumberUtils.readDecimalValue(x, readMethod))
                .max(Comparator.comparing(x -> (BigDecimal) x));
        return maxValue.isPresent() ? maxValue.get() : BigDecimal.ZERO;
    }
}
