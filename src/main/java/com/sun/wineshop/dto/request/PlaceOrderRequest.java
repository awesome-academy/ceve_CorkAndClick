package com.sun.wineshop.dto.request;

public record PlaceOrderRequest(
        Long userId,
        String recipientName,
        String address,
        String phoneNumber
) {}
