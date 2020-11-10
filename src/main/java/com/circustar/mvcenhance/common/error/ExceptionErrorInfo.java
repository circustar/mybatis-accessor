package com.circustar.mvcenhance.common.error;

@Deprecated
public class ExceptionErrorInfo implements IErrorInfo<Exception> {
    public ErrorType getErrorType() {
        return ErrorType.Exception;
    }

    public Exception getErrorDetail() {
        return errorDetail;
    }

    @Override
    public String parseError() {
        return errorDetail.getMessage();
    }

    private Exception errorDetail;

    public void setErrorDetail(Exception errorDetail) {
        this.errorDetail = errorDetail;
    }

    public ExceptionErrorInfo(Exception errorDetail) {
        this.errorDetail = errorDetail;
    }
}
