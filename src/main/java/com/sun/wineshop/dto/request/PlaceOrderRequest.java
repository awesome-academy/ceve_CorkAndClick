package com.sun.wineshop.dto.request;

public record PlaceOrderRequest(
        String recipientName,
        String address,
        String phoneNumber
) {}
