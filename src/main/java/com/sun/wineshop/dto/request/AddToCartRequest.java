package com.sun.wineshop.dto.request;

public record AddToCartRequest(
    Long productId,
    int quantity
) {}
