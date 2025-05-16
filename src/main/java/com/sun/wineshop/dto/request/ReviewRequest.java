package com.sun.wineshop.dto.request;

public record ReviewRequest(
        Long productId,
        int rating,
        String comment
) {}
