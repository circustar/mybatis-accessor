package com.circustar.common_utils.collection;

import com.circustar.common_utils.reflection.FieldUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class NumberUtils {
    public static BigDecimal sumListByType(Class clazz, List list, Method valueReadMethod) {
        if(list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List numberList = (List) list.stream().map(x -> FieldUtils.getFieldValue(x, valueReadMethod)).collect(Collectors.toList());
        return sumNumberListByType(clazz, numberList);
    }

    public static BigDecimal sumNumberListByType(Class clazz, List numberList) {
        BigDecimal result = null;
        if(BigDecimal.class.isAssignableFrom(clazz)) {
            BigDecimal res = (BigDecimal) numberList.stream().reduce(
                    (x, y) -> (x == null ? BigDecimal.ZERO:((BigDecimal)x)).add(y == null ? BigDecimal.ZERO:((BigDecimal)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = res;
        } else if(Double.class.isAssignableFrom(clazz)) {
            Double res = (Double) numberList.stream().reduce((x, y) -> (x == null ? 0:((Double)x)) + (y == null ? 0:((Double)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Integer.class.isAssignableFrom(clazz)) {
            Integer res = (Integer) numberList.stream().reduce((x, y) -> (x == null ? 0:((Integer)x)) + (y == null ? 0:((Integer)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Long.class.isAssignableFrom(clazz)) {
            Long res = (Long) numberList.stream().reduce((x, y) -> (x == null ? 0:((Long)x)) + (y == null ? 0:((Long)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Float.class.isAssignableFrom(clazz)) {
            Float res = (Float) numberList.stream().reduce((x, y) -> (x == null ? 0:((Float)x)) + (y == null ? 0:((Float)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Short.class.isAssignableFrom(clazz)) {
            Short res = (Short) numberList.stream().reduce((x, y) -> (x == null ? 0:((Short)x)) + (y == null ? 0:((Short)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        }  else {
            throw new RuntimeException("not support type for summary : " + clazz.getSimpleName());
        }
        return result;
    }

    public static BigDecimal readDecimalValue(Class clazz, Object obj, Method readMethod) {
        BigDecimal result = null;
        if(BigDecimal.class.isAssignableFrom(clazz)) {
            BigDecimal res = (BigDecimal)FieldUtils.getFieldValue(obj, readMethod);
            if(res == null) { return BigDecimal.ZERO; }
            result = res;
        } else if(Double.class.isAssignableFrom(clazz)) {
            Double res = (Double)FieldUtils.getFieldValue(obj, readMethod);
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Integer.class.isAssignableFrom(clazz)) {
            Integer res = (Integer)FieldUtils.getFieldValue(obj, readMethod);
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Long.class.isAssignableFrom(clazz)) {
            Long res = (Long)FieldUtils.getFieldValue(obj, readMethod);
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Float.class.isAssignableFrom(clazz)) {
            Float res = (Float)FieldUtils.getFieldValue(obj, readMethod);
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else if(Short.class.isAssignableFrom(clazz)) {
            Short res = (Short)FieldUtils.getFieldValue(obj, readMethod);
            if(res == null) { return BigDecimal.ZERO; }
            result = BigDecimal.valueOf(res);
        } else {
            throw new RuntimeException("not support type for summary : " + clazz.getSimpleName());
        }
        return result;
    }

    public static Object castFromBigDecimal(Class clazz, BigDecimal value) {
        if(BigDecimal.class.isAssignableFrom(clazz)) {
            return value;
        } else if(Double.class.isAssignableFrom(clazz)) {
            return value.doubleValue();
        } else if(Integer.class.isAssignableFrom(clazz)) {
            return value.intValue();
        } else if(Long.class.isAssignableFrom(clazz)) {
            return value.longValue();
        } else if(Float.class.isAssignableFrom(clazz)) {
            return value.floatValue();
        } else if(Short.class.isAssignableFrom(clazz)) {
            return value.shortValue();
        }
        throw new RuntimeException("not support type for summary : " + clazz.getSimpleName());
    }

    public static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

}
