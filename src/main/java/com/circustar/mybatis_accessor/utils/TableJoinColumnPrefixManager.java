package com.circustar.mybatis_accessor.utils;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class TableJoinColumnPrefixManager {
    private final static String ALIAS_PREFIX = "z";
    private static AtomicLong tableIndex = new AtomicLong(0);
    private final static Map<String, String> TABLE_JOIN_COLUMN_PREFIX_MAP = new ConcurrentHashMap<>();

    public static String tryGet(Class<?> entityClass, Class<?> joinClass, int pos) {
        String fullName = entityClass.getName() + " " + joinClass.getName() + " " + pos;
        if(!TABLE_JOIN_COLUMN_PREFIX_MAP.containsKey(fullName)) {
            TABLE_JOIN_COLUMN_PREFIX_MAP.put(fullName, ALIAS_PREFIX + new BigInteger(String.valueOf(tableIndex.addAndGet(1)))
                    .toString(32));
        }
        return TABLE_JOIN_COLUMN_PREFIX_MAP.get(fullName);
    }
}
