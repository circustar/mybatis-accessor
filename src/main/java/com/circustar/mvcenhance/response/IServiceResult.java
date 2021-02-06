package com.circustar.mvcenhance.response;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;

public interface IServiceResult<T> {
    void setData(T data);
    T getData();
    default boolean containValidateErrors() {
        return getFieldErrorList() != null && getFieldErrorList().size() > 0;
    };

    void setGlobalErrorList(List<ObjectError> errorList);
    List<ObjectError> getGlobalErrorList();
    void addGlobalErrorList(List<ObjectError> errorList);

    void setFieldErrorList(List<FieldError> errorList);
    List<FieldError> getFieldErrorList();
    void addFieldErrorList(List<FieldError> errorList);

}
