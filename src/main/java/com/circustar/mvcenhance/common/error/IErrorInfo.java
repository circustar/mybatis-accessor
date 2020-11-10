package com.circustar.mvcenhance.common.error;

@Deprecated
public interface IErrorInfo<T> {
    ErrorType getErrorType();
    T getErrorDetail();
    String parseError();
}
