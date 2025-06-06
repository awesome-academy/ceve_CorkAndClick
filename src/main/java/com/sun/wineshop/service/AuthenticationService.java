package com.sun.wineshop.service;

import com.nimbusds.jose.JOSEException;
import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.request.LogoutRequest;
import com.sun.wineshop.dto.request.VerifyTokenRequest;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.dto.response.VerifyTokenResponse;

import java.text.ParseException;

public interface AuthenticationService {
    VerifyTokenResponse verifyToken(VerifyTokenRequest request) throws JOSEException, ParseException;

     LoginResponse login(LoginRequest request);

     void logout(LogoutRequest request)throws JOSEException, ParseException ;

     void activateAccount(String token);
}
