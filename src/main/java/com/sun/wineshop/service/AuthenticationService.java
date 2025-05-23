package com.sun.wineshop.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.wineshop.configuration.SecurityProperties;
import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.request.LogoutRequest;
import com.sun.wineshop.dto.request.VerifyTokenRequest;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.dto.response.VerifyTokenResponse;
import com.sun.wineshop.model.entity.InvalidatedToken;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.VerificationToken;
import com.sun.wineshop.repository.InvalidatedTokenRepository;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final SecurityProperties securityProperties;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private static final String CLAIM_SCOPE = "scope";

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final VerificationTokenRepository verificationTokenRepository;

    public VerifyTokenResponse verifyToken(VerifyTokenRequest request)
            throws JOSEException, ParseException {
        boolean isValid = true;
        try {
            checkValidToken(request.token());
        } catch (AppException e) {
            isValid = false;
        }

        return new VerifyTokenResponse(isValid);
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

    public void logout(LogoutRequest request) throws JOSEException, ParseException {
        var signToken = checkValidToken(request.token());
        String jid = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jid)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    public void activateAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        User user = verificationToken.getUser();
        user.setActive(true);

        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    private SignedJWT checkValidToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(securityProperties.getJwt().getSignerKey().getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expiration.after(new Date())))
            throw new AppException(ErrorCode.INVALID_TOKEN);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.INVALID_TOKEN);

        return signedJWT;
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
                .claim("userId", user.getId())
                .jwtID(UUID.randomUUID().toString())
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
