package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.entity.User;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private UserResponse mapToResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setAddress(user.getAddress());
        userResponse.setBirthday(user.getBirthday());
        userResponse.setPassword(user.getPassword());
        return userResponse;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsUserByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBirthday(request.getBirthday());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setActive(true);
        user.setRole(1);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToResponse);
    }
}
