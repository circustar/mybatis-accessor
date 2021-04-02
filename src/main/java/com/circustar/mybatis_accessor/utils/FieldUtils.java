package com.circustar.mybatis_accessor.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class FieldUtils {
    public static String parseClassName(String name) {
        return Arrays.stream(name.trim().replace('-', '_').split("_"))
                .map(x -> x.substring(0,1).toUpperCase() + x.substring(1))
                .collect(Collectors.joining());
    }

//    public static String unCap(String name) {
//        if(StringUtils.isEmpty(name)) {
//            return name;
//        }
//        return name.substring(0,1).toLowerCase() + name.substring(1);
//
//    }

//    public static void setField(Object obj, Field field, Object value) throws IllegalAccessException {
//        field.setAccessible(true);
//        field.set(obj, value);
//    }

    public static Object getFieldValue(Object obj, Method readMethod) throws InvocationTargetException, IllegalAccessException {
        if(obj != null && readMethod != null) {
            readMethod.setAccessible(true);
            return readMethod.invoke(obj);
        }
        return null;
    }

    public static void setFieldValue(Object obj, Method writeMethod, Object value) throws IllegalAccessException, InvocationTargetException {
        if(writeMethod == null || obj == null) {
            return;
        }
        writeMethod.setAccessible(true);
        writeMethod.invoke(obj, value);
    }

//    public static void setFieldByName(Object obj, String name, Object value) throws IllegalAccessException, InvocationTargetException {
//        if(obj == null) {
//            return;
//        }
//        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(obj.getClass(), name);
//        if(propertyDescriptor != null) {
//            Method writeMethod = propertyDescriptor.getWriteMethod();
//            if(writeMethod != null) {
//                writeMethod.setAccessible(true);
//                writeMethod.invoke(obj, value);
//            }
//        }
//        return;
//    }

//    public static void setFieldByNameWithCollection(Object object, String name, Object value) throws IllegalAccessException, InvocationTargetException {
//        if(object == null) {return;}
//        if(Collection.class.isAssignableFrom(object.getClass())) {
//            Collection c = (Collection)object;
//            for (Object obj : c) {
//                setFieldByName(obj, name, value);
//            }
//        } else {
//            setFieldByName(object, name, value);
//        }
//    }

//    public static Object getValueByName(Object obj, String name) throws IllegalAccessException, InvocationTargetException {
//        if(obj == null) {
//            return null;
//        }
//        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(obj.getClass(), name);
//        if(propertyDescriptor != null) {
//            Method readMethod = propertyDescriptor.getReadMethod();
//            if(readMethod != null) {
//                readMethod.setAccessible(true);
//                return readMethod.invoke(obj);
//            }
//        }
//        return null;
//    }

//    public static void setValue(Object obj, Method writeMethod, Object value) throws InvocationTargetException, IllegalAccessException {
//        writeMethod.setAccessible(true);
//        writeMethod.invoke(obj, value);
//    }

//    public static List<Field> getExistFields(Object obj, List<String> names, boolean throwable) throws NoSuchFieldException {
//        List<Field> fields = new ArrayList<>();
//        for(String name : names) {
//            try {
//                Field f = obj.getClass().getDeclaredField(name);
//                fields.add(f);
//            } catch (NoSuchFieldException ex) {
//                if(throwable) {
//                    throw ex;
//                }
//            }
//        }
//        return fields;
//    }

}
