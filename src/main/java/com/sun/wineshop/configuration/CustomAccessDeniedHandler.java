package com.sun.wineshop.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.utils.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final MessageUtil messageUtil;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseApiResponse<Void> responseBody = new BaseApiResponse<>(
                errorCode.getCode(),
                messageUtil.getMessage(errorCode.getMessageKey())
        );

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
        response.flushBuffer();
    }
}
