package com.sun.wineshop.dto.response;

import java.time.LocalDateTime;

public record UserResponse (
    Long id,
    String username,
    String fullName,
    String email,
    String phone,
    String address,
    LocalDateTime birthday
) {}
