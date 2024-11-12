package com.circustar.common_utils.collection;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {
    public static String DELIMITER_IN_SQL = ".";
    public static String c2l(String str) {
        if(!org.springframework.util.StringUtils.hasLength(str)) {
            return str;
        }

        String[] splitString = str.split("\\" + DELIMITER_IN_SQL);
        int size = splitString.length - 1;
        String suffix = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(splitString[size]);
        String prefix = "";
        if(size > 0) {
            prefix = Arrays.stream(splitString).limit(size).collect(Collectors.joining(DELIMITER_IN_SQL)) + DELIMITER_IN_SQL;
        }
        return prefix + suffix;
    }

}
