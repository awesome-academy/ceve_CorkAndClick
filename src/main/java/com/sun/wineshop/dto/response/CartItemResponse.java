package com.sun.wineshop.dto.response;

import lombok.Builder;

@Builder
public record CartItemResponse(
    Long productId,
    String productName,
    String imageUrl,
    Double price,
    int quantity,
    boolean isAvailable
) {}
