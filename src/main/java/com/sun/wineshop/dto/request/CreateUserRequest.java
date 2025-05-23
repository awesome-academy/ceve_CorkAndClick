package com.sun.wineshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateUserRequest(
        @NotBlank(message = "USERNAME_BLANK")
        @Size(min = 3, message = "USERNAME_INVALID_SIZE")
        String username,
        @Size(min = 8, message = "PASSWORD_INVALID_SIZE")
        String password,
        String fullName,
        String email,
        String phone,
        String address,
        LocalDateTime birthday
) {
}
