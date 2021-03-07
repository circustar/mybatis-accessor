package com.circustar.mybatisAccessor.utils;

import java.util.HashMap;
import java.util.Map;

public class MapOptionUtils {
    public static <T> T getValue(Map map, Object key, T defaultValue) {
        if(map == null  || key == null) {
            return defaultValue;
        }
        if(map.containsKey(key)) {
            T res = (T) map.get(key);
            return res == null? defaultValue : res;
        }
        return defaultValue;
    }
    public static Map copy(Map map) {
        if(map == null) {
            return null;
        }
        Map newMap = new HashMap();
        newMap.putAll(map);
        return newMap;
    }
}
