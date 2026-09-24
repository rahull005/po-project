package com.example.po.flex.infrastructure.exception;

public abstract class FlexException
        extends RuntimeException {

    protected FlexException(
            String message) {

        super(message);
    }

    protected FlexException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
