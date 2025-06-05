package com.sun.wineshop.dto.request;

import lombok.Builder;

@Builder
public record UpdateCartItemRequest(
    Long productId,
    int quantity
) {}
