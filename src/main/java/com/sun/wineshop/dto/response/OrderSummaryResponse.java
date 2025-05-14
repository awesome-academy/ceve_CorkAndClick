package com.sun.wineshop.dto.response;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long orderId,
        Double totalAmount,
        String status,
        LocalDateTime createdAt
) {}
