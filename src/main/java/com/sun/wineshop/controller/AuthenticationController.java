package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public BaseApiResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse result = authenticationService.login(loginRequest);
        BaseApiResponse<LoginResponse> response = new BaseApiResponse<>();
        response.setData(result);
        return response;
    }
}
