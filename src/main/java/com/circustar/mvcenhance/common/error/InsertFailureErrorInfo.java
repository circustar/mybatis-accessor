package com.circustar.mvcenhance.common.error;

public class InsertFailureErrorInfo implements IErrorInfo<String> {
    private static String ERROR_MESSAGE = "insert failed";

    public ErrorType getErrorType() {
        return ErrorType.InsertFailure;
    }

    public String getErrorDetail() {
        return ERROR_MESSAGE;
    }

    @Override
    public String parseError() {
        return ERROR_MESSAGE;
    }
}
