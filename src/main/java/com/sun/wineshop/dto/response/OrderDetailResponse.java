package com.sun.wineshop.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        Long userId,
        String recipientName,
        String address,
        String phoneNumber,
        String status,
        String rejectReason,
        double totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}
