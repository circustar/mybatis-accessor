package org.yxy.circustar.mvc.common.error;

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
