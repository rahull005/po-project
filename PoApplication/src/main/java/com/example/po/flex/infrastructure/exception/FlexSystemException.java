package com.example.po.flex.infrastructure.exception;

import com.example.po.flex.config.FlexConfig;

public class FlexSystemException extends FlexException{
    public FlexSystemException(String message,Throwable cause){
        super(message,cause);
    }
}
