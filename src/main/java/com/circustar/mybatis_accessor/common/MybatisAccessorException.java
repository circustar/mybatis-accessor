package com.circustar.mybatis_accessor.common;

public class MybatisAccessorException extends Exception {
    public enum ExceptionType {
        TARGET_NOT_FOUND,
        NOT_SUPPORT_TYPE,
        SQL_ERROR,
        METHOD_INVOKE_EXCEPTION;
    }

    private ExceptionType exceptionType;
    public MybatisAccessorException(ExceptionType type, Exception ex) {
        super(ex);
        this.exceptionType = type;
    }
    public MybatisAccessorException(ExceptionType type, String message) {
        super(message);
        this.exceptionType = type;
    }
    public ExceptionType getExceptionType() {
        return exceptionType;
    }
}
