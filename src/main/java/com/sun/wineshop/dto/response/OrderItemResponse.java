package com.sun.wineshop.dto.response;

public record OrderItemResponse(
        Long productId,
        String productName,
        int quantity,
        double unitPrice
) {}
