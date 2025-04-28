package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        BaseApiResponse<UserResponse> response = new BaseApiResponse<>();
        response.setData(userService.createUser(request));
        return response;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }
}
