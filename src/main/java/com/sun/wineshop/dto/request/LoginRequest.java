package com.sun.wineshop.dto.request;

public record LoginRequest (
    String username,
    String password
) {}
