package com.sun.wineshop.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.wineshop.configuration.SecurityProperties;
import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.request.LogoutRequest;
import com.sun.wineshop.dto.request.VerifyTokenRequest;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.dto.response.VerifyTokenResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.InvalidatedToken;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.model.entity.VerificationToken;
import com.sun.wineshop.repository.InvalidatedTokenRepository;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.repository.VerificationTokenRepository;
import com.sun.wineshop.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private SecurityProperties.Jwt jwtProps;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("hashedPass")
                .role("USER")
                .isActive(false)
                .build();
    }

    private void setupJwtProperties() {
        jwtProps = new SecurityProperties.Jwt();
        jwtProps.setSignerKey("lbMfTWNvhHb1dIkUFyn04zQxyqaVNSXmxdmaAv5IT97MIEQhY0ujJfqFeP/GcWBn");
        jwtProps.setDomain("localhost");

        when(securityProperties.getJwt()).thenReturn(jwtProps);
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        setupJwtProperties();
        LoginRequest request = new LoginRequest("testuser", "rawPass");

        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordService.matches("rawPass", "hashedPass")).thenReturn(true);

        LoginResponse response = authenticationService.login(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.token());
    }

    @Test
    void login_invalidPassword_shouldThrowAppException() {
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordService.matches(any(), any())).thenReturn(false);

        LoginRequest request = new LoginRequest("testuser", "wrongpass");

        AppException ex = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.LOGIN_FAILED, ex.getErrorCode());
    }

    @Test
    void login_userNotFound_shouldThrowAppException() {
        when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown", "pass");

        AppException ex = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.USER_NOT_EXIST, ex.getErrorCode());
    }

    @Test
    void verifyToken_valid_shouldReturnTrue() throws Exception {
        setupJwtProperties();
        String token = generateTestToken("1");

        VerifyTokenRequest request = new VerifyTokenRequest(token);

        boolean exists = false;
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(exists);

        VerifyTokenResponse response = authenticationService.verifyToken(request);
        assertTrue(response.isSuccess());
    }

    @Test
    void verifyToken_invalid_shouldReturnFalse() throws JOSEException, ParseException {
        setupJwtProperties();

        String invalidToken = generateInvalidSignedToken("wrong-jwt-id");

        VerifyTokenRequest request = new VerifyTokenRequest(invalidToken);

        VerifyTokenResponse response = authenticationService.verifyToken(request);

        assertFalse(response.isSuccess());
    }

    @Test
    void logout_validToken_shouldSaveInvalidatedToken() throws Exception {
        setupJwtProperties();
        String token = generateTestToken("jwt-id-1");
        LogoutRequest request = new LogoutRequest(token);

        authenticationService.logout(request);

        verify(invalidatedTokenRepository).save(any(InvalidatedToken.class));
    }

    @Test
    void activateAccount_validToken_shouldActivateUser() {
        VerificationToken token = VerificationToken.builder()
                .token("abc123")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();

        when(verificationTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        authenticationService.activateAccount("abc123");

        assertTrue(user.isActive());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(token);
    }

    @Test
    void activateAccount_expired_shouldThrowAppException() {
        VerificationToken token = VerificationToken.builder()
                .token("abc123")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .user(user)
                .build();

        when(verificationTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.activateAccount("abc123"));

        assertEquals(ErrorCode.INVALID_VERIFICATION_TOKEN, ex.getErrorCode());
    }

    // Utility: generate a signed JWT for testing
    private String generateTestToken(String jwtId) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("testuser")
                .expirationTime(new Date(System.currentTimeMillis() + 100000))
                .jwtID(jwtId)
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(new MACSigner(jwtProps.getSignerKey().getBytes()));
        return signedJWT.serialize();
    }

    private String generateInvalidSignedToken(String jwtId) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("testuser")
                .expirationTime(new Date(System.currentTimeMillis() + 100000))
                .jwtID(jwtId)
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claims);

        signedJWT.sign(new MACSigner("wrong-secret-key-12345678901234567890123456789012345678901234567890".getBytes()));
        return signedJWT.serialize();
    }
}
