package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.LoginRequest;
import com.sun.wineshop.dto.response.LoginResponse;
import com.sun.wineshop.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSuccess(passwordEncoder.matches(request.getPassword(), user.getPassword()));
        return loginResponse;
    }
}
