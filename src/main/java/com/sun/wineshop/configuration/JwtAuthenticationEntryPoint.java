package com.sun.wineshop.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.utils.MessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageUtil messageUtil;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseApiResponse<?> responseBody = new BaseApiResponse<>(
                errorCode.getCode(),
                messageUtil.getMessage(errorCode.getMessageKey())
        );

        ObjectMapper objectMapper = new ObjectMapper();

        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
        response.flushBuffer();
    }
}
