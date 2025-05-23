package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserByUserId(Long userId);
    UserResponse getCurrentUser();
}
