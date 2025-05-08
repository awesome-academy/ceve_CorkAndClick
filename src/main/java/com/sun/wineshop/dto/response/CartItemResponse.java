package com.sun.wineshop.dto.response;

public record CartItemResponse(
    Long productId,
    String productName,
    String imageUrl,
    Double price,
    int quantity
) {}
