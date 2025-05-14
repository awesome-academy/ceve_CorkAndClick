package com.sun.wineshop.utils;

import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtUtil {
    public static Long extractUserIdFromJwt(Jwt jwt) {
        Long userId = jwt.getClaim(AppConstants.JWT_USER_ID);
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
