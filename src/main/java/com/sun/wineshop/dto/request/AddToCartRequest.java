package com.sun.wineshop.dto.request;

public record AddToCartRequest(
    Long userId,
    Long productId,
    int quantity
) {}
