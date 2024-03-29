package com.circustar.common_utils.collection;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NumberUtils {
    public static BigDecimal sumListByType(final Class clazz, final List list, final Method valueReadMethod) throws MybatisAccessorException {
        if(list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List numberList = (List) list.stream().map(x -> FieldUtils.getFieldValue(x, valueReadMethod)).collect(Collectors.toList());
        return sumNumberListByType(clazz, numberList);
    }

    public static BigDecimal sumNumberListByType(final Class clazz, final List numberList) throws MybatisAccessorException {
        BigDecimal result;
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
        } else if(BigInteger.class.isAssignableFrom(clazz)) {
            BigInteger res = (BigInteger) numberList.stream().reduce((x, y) -> (x == null ? BigInteger.ZERO:((BigInteger)x)).add(y == null ? BigInteger.ZERO:((BigInteger)y))).get();
            if(res == null) { return BigDecimal.ZERO; }
            result = new BigDecimal(res);
        }  else {
            throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.NOT_SUPPORT_TYPE
                    ,"not support type for summary : " + clazz.getSimpleName());
        }
        return result;
    }

    public static BigDecimal readDecimalValue(final Object obj, final Method readMethod) throws MybatisAccessorException {
        final Object fieldValue = FieldUtils.getFieldValue(obj, readMethod);
        return castToBigDecimal(fieldValue);
    }
    public static BigDecimal castToBigDecimal(final Object obj) throws MybatisAccessorException {
        if(obj == null) {
            return BigDecimal.ZERO;
        }
        Class clazz = obj.getClass();
        BigDecimal result;
        if(BigDecimal.class.isAssignableFrom(clazz)) {
            result = (BigDecimal)obj;
        } else if(Double.class.isAssignableFrom(clazz)) {
            result = BigDecimal.valueOf((Double)obj);
        } else if(Integer.class.isAssignableFrom(clazz)) {
            result = BigDecimal.valueOf((Integer)obj);
        } else if(Long.class.isAssignableFrom(clazz)) {
            result = BigDecimal.valueOf((Long)obj);
        } else if(Float.class.isAssignableFrom(clazz)) {
            result = BigDecimal.valueOf((Float)obj);
        } else if(Short.class.isAssignableFrom(clazz)) {
            result = BigDecimal.valueOf((Short)obj);
        } else if(BigInteger.class.isAssignableFrom(clazz)) {
            result = new BigDecimal((BigInteger)obj);
        } else {
            throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.NOT_SUPPORT_TYPE
                    , "not support type for summary : " + clazz.getSimpleName());
        }
        return result;
    }

    public static Object castFromBigDecimal(final Class clazz, final BigDecimal value) throws MybatisAccessorException {
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
        } else if(BigInteger.class.isAssignableFrom(clazz)) {
            return value.toBigInteger();
        }
        throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.NOT_SUPPORT_TYPE
                , "not support type for summary : " + clazz.getSimpleName());
    }

    public static boolean isNumber(final String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

}
