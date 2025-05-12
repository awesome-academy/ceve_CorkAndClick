package com.sun.wineshop.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.wineshop.configuration.SecurityProperties;
import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.request.VerifyTokenRequest;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.dto.response.VerifyTokenResponse;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final SecurityProperties securityProperties;
    private static final String CLAIM_SCOPE = "scope";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordService passwordService;

    public VerifyTokenResponse verifyToken(VerifyTokenRequest request)
            throws JOSEException, ParseException {

        String token = request.token();

        JWSVerifier verifier = new MACVerifier(securityProperties.getJwt().getSignerKey().getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);
        return new VerifyTokenResponse(verified && expiration.after(new Date()));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        boolean isSuccess = passwordService.matches(request.password(), user.getPassword());
        if (!isSuccess)
            throw new AppException(ErrorCode.LOGIN_FAILED);
        String token = generateToken(user);

        return new LoginResponse(true, token);
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(securityProperties.getJwt().getDomain())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim(CLAIM_SCOPE, user.getRole())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(securityProperties.getJwt().getSignerKey().getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.CAN_NOT_CREATE_TOKEN);
        }
    }
}
