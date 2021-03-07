package com.circustar.mybatis_accessor.error;

import org.springframework.validation.BindingResult;

public class ValidateException extends Exception {
    private BindingResult bindingResult;
    public ValidateException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
