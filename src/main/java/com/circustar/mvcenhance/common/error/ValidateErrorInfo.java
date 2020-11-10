package com.circustar.mvcenhance.common.error;

import org.springframework.validation.FieldError;

import java.util.List;

@Deprecated
public class ValidateErrorInfo implements IErrorInfo<List<FieldError>> {
    public ErrorType getErrorType() {
        return ErrorType.FieldError;
    }

    public List<FieldError> getErrorDetail() {
        return errorDetail;
    }

    @Override
    public String parseError() {
        return message;
    }

    private String message;
    private List<FieldError> errorDetail;

    public void setErrorDetail(List<FieldError> errorDetail) {
        this.errorDetail = errorDetail;
    }

    public ValidateErrorInfo(String message, List<FieldError> errorDetail) {
        this.message = message;
        this.errorDetail = errorDetail;
    }
}
