package com.sun.wineshop.dto.request;

public record ReviewRequest(
        int rating,
        String comment
) {}
