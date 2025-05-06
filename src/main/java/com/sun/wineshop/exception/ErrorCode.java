package com.sun.wineshop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED(9999, "An unexpected error occurred"),
    INVALID_ERROR_KEY(9998, "Invalid error key!"),
    USER_EXISTED(40100, "User is existed!"),
    USERNAME_INVALID_SIZE(40101, "User name must be at least 3 characters!"),
    USERNAME_BLANK(40102, "User name can not be blank!"),
    PASSWORD_INVALID_SIZE(40103, "Password must be at least 8 characters!"),
    USER_NOT_EXIST(40104, "User not exist!"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Product not found.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = code;
        this.message = message;
    }

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
