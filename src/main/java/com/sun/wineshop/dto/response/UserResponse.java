package com.sun.wineshop.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse (
    Long id,
    String username,
    String fullName,
    String email,
    String phone,
    String address,
    LocalDateTime birthday,
    String role
) {}
