package com.sun.wineshop.dto.response;

import java.util.List;

public record OrderResponse(
        Long orderId,
        Double totalAmount,
        String status,
        List<OrderItemResponse> items
) {}
