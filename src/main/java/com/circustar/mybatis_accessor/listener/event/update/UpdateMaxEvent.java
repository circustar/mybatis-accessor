package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

public class UpdateMaxEvent extends UpdateAnyEvent implements IUpdateEvent<UpdateEventModel> {
    public UpdateMaxEvent(MybatisAccessorService mybatisAccessorService) {
        super(mybatisAccessorService);
    }

    @Override
    protected Comparable getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object subFieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        List valueList = CollectionUtils.convertToList(subFieldValue);
        DtoField updateField = dtoFields.get(2);
        Method readMethod = updateField.getPropertyDescriptor().getReadMethod();
        return (Comparable) valueList.stream().map(x -> FieldUtils.getFieldValue(x, readMethod))
                .filter(x -> x != null)
                .max(Comparator.comparing(x -> (Comparable) x)).orElse(null);
    }
}
