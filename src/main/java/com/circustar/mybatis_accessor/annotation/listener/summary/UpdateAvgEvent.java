package com.circustar.mybatis_accessor.annotation.listener.summary;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.listener.IUpdateEvent;
import com.circustar.mybatis_accessor.annotation.listener.UpdateEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class UpdateAvgEvent extends UpdateSumEvent implements IUpdateEvent<UpdateEventModel> {

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields
            , DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        String precision = updateEventModel.getUpdateParams().get(3);
        List<Object> result = new ArrayList<>();
        result.add(Integer.valueOf(precision));
        return result;
    }

    @Override
    protected BigDecimal getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object subFieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        List valueList = CollectionUtils.convertToList(subFieldValue);
        DtoField sumField = dtoFields.get(2);
        Class type = sumField.getActualClass();
        Method readMethod = sumField.getPropertyDescriptor().getReadMethod();
        return NumberUtils.sumListByType(type, valueList, readMethod)
                .divide(BigDecimal.valueOf(valueList.size()), (int)parsedParams.get(0), RoundingMode.HALF_DOWN);
    }
}
