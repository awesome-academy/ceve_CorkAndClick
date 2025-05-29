package com.sun.wineshop.dto.websocket;

public record OrderNotification(
        Long orderId,
        String status,
        Double totalAmount,
        String message
) {}
