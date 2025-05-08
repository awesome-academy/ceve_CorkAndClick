package com.sun.wineshop.dto.request;

public record UpdateCartItemRequest(
    Long userId,
    Long productId,
    int quantity
) {}
