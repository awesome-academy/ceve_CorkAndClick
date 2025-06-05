package com.sun.wineshop.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CartResponse(
    Long cartId,
    Long userId,
    List<CartItemResponse> items,
    Double totalAmount
) {}
