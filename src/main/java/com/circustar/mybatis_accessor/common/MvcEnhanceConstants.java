package com.circustar.mybatis_accessor.common;

public final class MvcEnhanceConstants {
    public final static String ID_REFERENCE_NOT_FOUND = "id reference not found, Class : %s, field : %s";
    public final static String DTO_NAME_NOT_FOUND = "DTO NAME NOT FOUND : %s";
    public final static String UPDATE_TARGET_NOT_FOUND = "UPDATE TARGET NOT FOUND : %s";
    public final static String MYBATIS_ENHANCE_JOIN_TABLE = "__MYBATIS_ENHANCE_JOIN_TABLE";
    public final static String MYBATIS_ENHANCE_JOIN_COLUMNS = "__MYBATIS_ENHANCE_JOIN_COLUMNS";

    public static class TargetNotFoundException extends Exception {
    }

    public static class ListIsEmptyException extends Exception {
    }

    public static class NotSupportTypeException extends Exception {
    }
}
