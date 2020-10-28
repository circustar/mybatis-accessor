package com.circustar.mvcenhance.common.error;

public class ResourceNotFoundErrorInfo implements IErrorInfo<String> {
    private static String ERROR_MESSAGE = "request resource not found:";
    public ErrorType getErrorType() {
        return ErrorType.ResourceNotFound;
    }

    public String getErrorDetail() {
        return ERROR_MESSAGE + resourceName;
    }

    private String resourceName;

    @Override
    public String parseError() {
        return ERROR_MESSAGE + resourceName;
    }

    public ResourceNotFoundErrorInfo(String resourceName) {
        this.resourceName = resourceName;
    }

}
