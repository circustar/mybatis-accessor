package com.circustar.mybatis_accessor.utils;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TableJoinColumnPrefixManager {
    private final static String aliasPrefix = "z";
    private static AtomicLong tableIndex = new AtomicLong(0);

    private final static Map<String, Long> tableJoinColumnPrefixMap = new ConcurrentHashMap<>();

    public static String tryGet(Class<?> entityClass, String fieldName) {
        String fullName = entityClass.getName() + "." + fieldName;
        try {
            if(!tableJoinColumnPrefixMap.containsKey(fullName)) {
                tableJoinColumnPrefixMap.put(fullName, tableIndex.addAndGet(1));
            }
        } catch (Exception ex) {
        }
        return aliasPrefix + new BigInteger(tableJoinColumnPrefixMap.get(fullName).toString())
                .toString(32);
    }
}
