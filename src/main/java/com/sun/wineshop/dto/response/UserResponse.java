package com.sun.wineshop.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String password;
    private LocalDateTime birthday;
}
