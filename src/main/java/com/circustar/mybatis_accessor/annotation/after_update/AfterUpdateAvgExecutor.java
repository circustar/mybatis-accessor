package com.circustar.mybatis_accessor.annotation.after_update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AfterUpdateAvgExecutor extends AfterUpdateSumExecutor implements  IAfterUpdateExecutor {

    @Override
    protected List<Object> parseParams(List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, String[] originParams) {
        String precision = originParams[3];
        List<Object> result = new ArrayList<>();
        result.add(Integer.valueOf(precision));
        return result;
    }

    @Override
    protected BigDecimal getValue(QueryWrapper queryWrapper, IService fieldServiceBean, List<DtoField> dtoFields, List<Object> parsedParams) {
        EntityFieldInfo entityFieldInfo = dtoFields.get(2).getEntityFieldInfo();
        Method readMethod = entityFieldInfo.getPropertyDescriptor().getReadMethod();
        List valueList = fieldServiceBean.list(queryWrapper);
        if(valueList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Class<?> type = entityFieldInfo.getField().getType();
        BigDecimal sumValue = NumberUtils.sumListByType(type, valueList, readMethod);
        return sumValue.divide(BigDecimal.valueOf(valueList.size()), (int)parsedParams.get(0), RoundingMode.HALF_DOWN);
    }
}
