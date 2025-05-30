package com.sun.wineshop.dto.request;

public record MessageRequest(
        String content,
        String sender,
        Long userId
) {}
