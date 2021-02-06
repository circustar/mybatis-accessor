package com.circustar.mvcenhance.response;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;

public class DefaultServiceResult<T> implements IServiceResult<T> {
    private T data;
    private List<FieldError> errorList;
    private List<ObjectError> globalErrorList;
    @Override
    public void setData(T data) {
        this.data = data;
    }


    @Override
    public T getData() {
        return data;
    }

    @Override
    public void setGlobalErrorList(List<ObjectError> errorList) {
        this.globalErrorList  = errorList;
    }

    @Override
    public List<ObjectError> getGlobalErrorList() {
        return this.globalErrorList;
    }

    @Override
    public void addGlobalErrorList(List<ObjectError> errorList) {
        if(this.globalErrorList == null) {
            this.globalErrorList = errorList;
        } else {
            this.globalErrorList.addAll(errorList);
        }
    }

    @Override
    public void setFieldErrorList(List<FieldError> errorList) {
        this.errorList = errorList;
    }

    @Override
    public List<FieldError> getFieldErrorList() {
        return errorList;
    }

    @Override
    public void addFieldErrorList(List<FieldError> errorList) {
        if(this.errorList == null) {
            this.errorList = errorList;
        } else {
            this.errorList.addAll(errorList);
        }
    }



}
