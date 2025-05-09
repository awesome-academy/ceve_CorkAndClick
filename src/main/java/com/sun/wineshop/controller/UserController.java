package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.service.UserService;
import com.sun.wineshop.utils.api.UserApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(UserApiPaths.BASE)
public class UserController {

    private final UserService userService;

    @PostMapping(UserApiPaths.Endpoint.REGISTER)
    public ResponseEntity<BaseApiResponse<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                userService.createUser(request)
        ));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                userService.getUserByUserId(id)
        ));
    }

    @GetMapping(UserApiPaths.Endpoint.INFO)
    public ResponseEntity<BaseApiResponse<UserResponse>> getInfoCurrentUser() {
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                userService.getCurrentUser()
        ));
    }
}
