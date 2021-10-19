package com.circustar.mybatis_accessor.annotation.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

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
    protected BigDecimal getValue(QueryWrapper queryWrapper, IService fieldServiceBean, List<DtoField> dtoFields, List<Object> parsedParams) {
        EntityFieldInfo entityFieldInfo = dtoFields.get(2).getEntityFieldInfo();
        Method readMethod = entityFieldInfo.getPropertyDescriptor().getReadMethod();
        List valueList = fieldServiceBean.list(queryWrapper);
        Class<?> type = entityFieldInfo.getField().getType();
        return NumberUtils.sumListByType(type, valueList, readMethod);
    }
}
