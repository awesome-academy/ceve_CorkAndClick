package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.service.UserService;
import com.sun.wineshop.utils.api.UserApiPaths;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UserApiPaths.BASE)
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(UserApiPaths.Endpoint.REGISTER)
    public ResponseEntity<BaseApiResponse<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        BaseApiResponse<UserResponse> response = new BaseApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setData(userService.createUser(request));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }
}
