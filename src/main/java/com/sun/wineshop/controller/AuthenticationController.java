package com.sun.wineshop.controller;

import com.nimbusds.jose.JOSEException;
import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.request.LogoutRequest;
import com.sun.wineshop.dto.request.VerifyTokenRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.dto.response.VerifyTokenResponse;
import com.sun.wineshop.service.AuthenticationService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AuthApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(AuthApiPaths.BASE)
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final MessageUtil messageUtil;

    @PostMapping(AuthApiPaths.Endpoint.LOGIN)
    public ResponseEntity<BaseApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                authenticationService.login(loginRequest)
        ));
    }

    @PostMapping(AuthApiPaths.Endpoint.VERIFY_TOKEN)
    public ResponseEntity<BaseApiResponse<VerifyTokenResponse>> verifyToken(@RequestBody VerifyTokenRequest request)
            throws JOSEException, ParseException {
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                authenticationService.verifyToken(request)
        ));
    }

    @PostMapping(AuthApiPaths.Endpoint.LOGOUT)
    public ResponseEntity<BaseApiResponse<Void>> logout(@RequestBody LogoutRequest request)
            throws JOSEException, ParseException {
        authenticationService.logout(request);

        return ResponseEntity.status(HttpStatus.OK).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        messageUtil.getMessage("auth.logout.success")
                )
        );
    }

    @GetMapping(AuthApiPaths.Endpoint.ACTIVATE)
    public ResponseEntity<BaseApiResponse<String>> activateAccount(@RequestParam String token) {
        authenticationService.activateAccount(token);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("auth.activate.account.success")
        ));
    }
}
