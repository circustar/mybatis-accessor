package org.yxy.circustar.mvc.common.error;

public class DeleteFailureErrorInfo implements IErrorInfo<String> {
    private static String ERROR_MESSAGE = "delete failed";

    public ErrorType getErrorType() {
        return ErrorType.DeleteFailure;
    }

    public String getErrorDetail() {
        return ERROR_MESSAGE;
    }

    @Override
    public String parseError() {
        return ERROR_MESSAGE;
    }
}
