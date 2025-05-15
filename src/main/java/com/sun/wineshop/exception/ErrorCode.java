package com.sun.wineshop.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED(999, "error.uncategorized"),
    INVALID_ERROR_KEY(998, "error.invalid.key"),
    USER_EXISTED(40100, "error.user.existed"),
    USERNAME_INVALID_SIZE(40101, "error.username.size"),
    USERNAME_BLANK(40102, "error.username.blank"),
    PASSWORD_INVALID_SIZE(40103, "error.password.size"),
    USER_NOT_EXIST(40104, "error.user.not.exist"),
    USER_NOT_FOUND_FROM_TOKEN(40105, "error.user.not.found.from.token"),
    CART_NOT_FOUND(404, "error.cart.not.found"),
    PRODUCT_NOT_FOUND_IN_CART(404, "error.product.not.in.cart"),
    LOGIN_FAILED(401, "error.login.failed"),
    CAN_NOT_CREATE_TOKEN(500, "error.can.not.create.token"),
    ACCESS_DENIED(403, "error.access.denied"),
    UNAUTHORIZED(401, "error.unauthorized"),
    CART_EMPTY(404, "error.cart.empty"),
    ORDER_NOT_FOUND(404, "error.order.not.found"),
    ORDER_CANNOT_BE_CANCELLED(400, "order.not.cancelled"),

    // Category
    CATEGORY_NAME_INVALID(40200, "error.category.name.invalid"),
    CATEGORY_DESCRIPTION_INVALID(40201, "error.category.description.size"),
    CATEGORY_SOME_NOT_FOUND(40202, "error.some.category.not.found"),
    CATEGORY_IN_USE(40203, "error.category.in.use"),
    CATEGORY_NOT_FOUND(40204, "error.category.not.found"),

    // Product
    PRODUCT_NAME_INVALID(40300, "error.product.name.invalid"),
    PRODUCT_DESCRIPTION_INVALID(40301, "error.product.description.invalid"),
    PRODUCT_IMAGE_URL_INVALID(40302, "error.product.image.url.invalid"),
    PRODUCT_PRICE_INVALID(40303, "error.product.price.invalid"),
    PRODUCT_ORIGIN_INVALID(40304, "error.product.origin.invalid"),
    PRODUCT_VOLUME_INVALID(40305, "error.product.volume.invalid"),
    PRODUCT_STOCK_INVALID(40306, "error.product.stock.invalid"),
    PRODUCT_ALCOHOL_PERCENTAGE_INVALID(40307, "error.product.alcohol.percentage.invalid"),
    PRODUCT_CATEGORIES_REQUIRED(40308, "error.product.categories.required"),
    PRODUCT_CATEGORY_ID_NULL(40309, "error.product.category.id.null"),
    PRODUCT_IN_USE(40310, "error.product.in.use"),
    PRODUCT_NOT_FOUND(40311, "error.product.not.found"),
    ;

    private final int code;
    private final String messageKey;

    ErrorCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
