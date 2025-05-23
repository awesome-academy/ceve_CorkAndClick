package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.VerificationToken;
import com.sun.wineshop.model.enums.UserRole;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.repository.VerificationTokenRepository;
import com.sun.wineshop.service.EmailService;
import com.sun.wineshop.service.PasswordService;
import com.sun.wineshop.service.UserService;
import com.sun.wineshop.utils.api.AuthApiPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsUserByUsername(request.username()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = User.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .birthday(request.birthday())
                .password(passwordService.encodePassword(request.password()))
                .isActive(false)
                .role(UserRole.USER.name())
                .build();
        User savedUser = userRepository.save(user);


        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        tokenRepository.save(verificationToken);

        String activationLink = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(AuthApiPaths.Endpoint.FULL_ACTIVATE)
                .queryParam("token", token)
                .build()
                .toUriString();

        emailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationLink);

        return ToDtoMappers.toUserResponse(savedUser);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        return users.map(ToDtoMappers::toUserResponse);
    }

    public UserResponse getUserByUserId(Long userId) {
        return ToDtoMappers.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST)));
    }

    public UserResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND_FROM_TOKEN));
        return ToDtoMappers.toUserResponse(user);
    }
}
