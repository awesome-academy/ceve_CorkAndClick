package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordService passwordService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return new LoginResponse(passwordService.matches(request.password(), user.getPassword()));
    }
}
