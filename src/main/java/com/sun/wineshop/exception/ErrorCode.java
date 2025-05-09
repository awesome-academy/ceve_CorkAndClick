package com.sun.wineshop.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED(9999, "error.uncategorized"),
    INVALID_ERROR_KEY(9998, "error.invalid.key"),
    USER_EXISTED(40100, "error.user.existed"),
    USERNAME_INVALID_SIZE(40101, "error.username.size"),
    USERNAME_BLANK(40102, "error.username.blank"),
    PASSWORD_INVALID_SIZE(40103, "error.password.size"),
    USER_NOT_EXIST(40104, "error.user.not.exist"),
    PRODUCT_NOT_FOUND(404, "error.product.not.found"),
    CART_NOT_FOUND(404, "error.cart.not.found"),
    PRODUCT_NOT_FOUND_IN_CART(404, "error.product.not.in.cart"),
    UNAUTHORIZED(401, "error.login.failed"),
    CAN_NOT_CREATE_TOKEN(401, "error.can.not.create.token"),;

    private final int code;
    private final String messageKey;

    ErrorCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
