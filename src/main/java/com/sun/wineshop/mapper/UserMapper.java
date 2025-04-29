package com.sun.wineshop.mapper;

import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.model.entity.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getBirthday()
        );
    }
}
