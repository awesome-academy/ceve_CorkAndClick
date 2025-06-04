package com.sun.wineshop.dto.request;

import lombok.Builder;

@Builder
public record AddToCartRequest(
    Long productId,
    int quantity
) {}
