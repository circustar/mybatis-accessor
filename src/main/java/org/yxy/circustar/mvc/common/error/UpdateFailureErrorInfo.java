package org.yxy.circustar.mvc.common.error;

public class UpdateFailureErrorInfo implements IErrorInfo<String> {
    private static String ERROR_MESSAGE = "update failed";

    public ErrorType getErrorType() {
        return ErrorType.UpdateFailure;
    }

    public String getErrorDetail() {
        return ERROR_MESSAGE;
    }

    @Override
    public String parseError() {
        return ERROR_MESSAGE;
    }
}
