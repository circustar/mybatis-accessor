package com.circustar.mybatis_accessor.utils;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TableJoinColumnPrefixManager {
    private final static String aliasPrefix = "z";
    private static AtomicLong tableIndex = new AtomicLong(0);

    private final static Map<String, String> tableJoinColumnPrefixMap = new ConcurrentHashMap<>();

    public static String tryGet(Class<?> entityClass, Class<?> joinClass, int pos) {
        String fullName = entityClass.getName() + " " + joinClass.getName() + " " + pos;
        try {
            if(!tableJoinColumnPrefixMap.containsKey(fullName)) {
                tableJoinColumnPrefixMap.put(fullName, aliasPrefix + new BigInteger(String.valueOf(tableIndex.addAndGet(1)))
                        .toString(32));
            }
        } catch (Exception ex) {
        }
        return tableJoinColumnPrefixMap.get(fullName);
    }
}
