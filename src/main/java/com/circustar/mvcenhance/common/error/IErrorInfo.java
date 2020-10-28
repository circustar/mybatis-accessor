package com.circustar.mvcenhance.common.error;

public interface IErrorInfo<T> {
    ErrorType getErrorType();
    T getErrorDetail();
    String parseError();
}
