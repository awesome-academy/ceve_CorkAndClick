package com.sun.wineshop.dto.response;

import java.util.List;

public record CartResponse(
    Long cartId,
    Long userId,
    List<CartItemResponse> items,
    Double totalAmount
) {}
