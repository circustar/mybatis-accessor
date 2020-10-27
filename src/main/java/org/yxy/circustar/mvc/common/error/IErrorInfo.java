package org.yxy.circustar.mvc.common.error;

public interface IErrorInfo<T> {
    ErrorType getErrorType();
    T getErrorDetail();
    String parseError();
}
