package com.circustar.mybatis_accessor.error;

public class ResourceNotFoundException extends Exception {
    public static String RESOURCE_NOT_FOUND_PREFIX = "resource not found : ";
    public ResourceNotFoundException(String message) {
        super(RESOURCE_NOT_FOUND_PREFIX + message);
    }
}
