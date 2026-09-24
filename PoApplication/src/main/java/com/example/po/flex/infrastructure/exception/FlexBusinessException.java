package com.example.po.flex.infrastructure.exception;

public class FlexBusinessException extends FlexException {

    private final String errorCode;

    public FlexBusinessException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
