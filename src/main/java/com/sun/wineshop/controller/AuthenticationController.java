package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.service.AuthenticationService;
import com.sun.wineshop.utils.api.AuthApiPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthApiPaths.BASE)
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(AuthApiPaths.Endpoint.LOGIN)
    public ResponseEntity<BaseApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse result = authenticationService.login(loginRequest);
        BaseApiResponse<LoginResponse> response = new BaseApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setData(result);
        return ResponseEntity.ok(response);
    }
}
