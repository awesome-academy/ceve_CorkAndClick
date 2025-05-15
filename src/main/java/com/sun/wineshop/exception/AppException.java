package com.sun.wineshop.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = new Object[]{};
    }

    public AppException(ErrorCode errorCode, Object... args) {
        super();
        this.errorCode = errorCode;
        this.args = args != null ? args : new Object[]{};
    }
}
