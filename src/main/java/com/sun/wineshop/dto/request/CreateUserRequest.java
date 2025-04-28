package com.sun.wineshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateUserRequest {
    @NotBlank(message = "USERNAME_BLANK")
    @Size(min = 3, message = "USERNAME_INVALID_SIZE")
    private String username;
    @Size(min = 8, message = "PASSWORD_INVALID_SIZE")
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime birthday;
}
