package com.circustar.mvcenhance.utils;

import org.springframework.util.StringUtils;

public class ArrayParamUtils {
    public static String DELIMITER_COMMA = ",";
    public static String DELIMITER_SEMICOLON = ";";
    public static <T> T parseArray(Object[] array, int position, T defaultValue) {
        if(array == null || array.length - 1 < position) {
            return defaultValue;
        }
        return (T)array[position];
    }
    public static String[] convertStringToArray(String str) {
        return convertStringToArray(str, DELIMITER_COMMA);
    }
    public static String[] convertStringToArray(String str, String delimiter) {
        if(StringUtils.isEmpty(str)) {
            return null;
        }
        if(!" ".equals(delimiter)) {
            str = str.replace(" ", "");
        }
        return str.split(delimiter);
    }
}
