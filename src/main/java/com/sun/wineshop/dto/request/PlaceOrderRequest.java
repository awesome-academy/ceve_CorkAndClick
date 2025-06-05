package com.sun.wineshop.dto.request;

import lombok.Builder;

@Builder
public record PlaceOrderRequest(
        String recipientName,
        String address,
        String phoneNumber
) {}
