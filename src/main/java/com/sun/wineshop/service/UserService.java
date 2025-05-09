package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.enums.UserRole;
import com.sun.wineshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordService passwordService;

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
                .role(UserRole.USER.name())
                .build();
        User savedUser = userRepository.save(user);

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
