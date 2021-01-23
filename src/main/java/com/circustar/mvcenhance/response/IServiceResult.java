package com.circustar.mvcenhance.response;

import org.springframework.validation.FieldError;

import java.util.List;

public interface IServiceResult<T> {
    void setData(T data);
    T getData();
    default boolean containValidateErrors() {
        return getFieldErrorList() != null && getFieldErrorList().size() > 0;
    };

    void setFieldErrorList(List<FieldError> errorList);
    List<FieldError> getFieldErrorList();
    void addFieldErrorList(List<FieldError> errorList);

}
