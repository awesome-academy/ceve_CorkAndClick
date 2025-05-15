package com.sun.wineshop.dto.request;

public record UpdateCartItemRequest(
    Long productId,
    int quantity
) {}
