package com.sun.wineshop.exception;

import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.utils.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        BaseApiResponse<Void> response = new BaseApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(messageUtil.getMessage(errorCode.getMessageKey()));

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String enumKey = (fieldError != null) ? fieldError.getDefaultMessage() : "UNCATEGORIZED";

        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (Exception ex) {
            errorCode = ErrorCode.INVALID_ERROR_KEY;
        }

        BaseApiResponse<Void> response = new BaseApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(messageUtil.getMessage(errorCode.getMessageKey()));

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseApiResponse<Void>> handleUnexpectedException(Exception e) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED;

        BaseApiResponse<Void> response = new BaseApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(messageUtil.getMessage(errorCode.getMessageKey()));

        return ResponseEntity.badRequest().body(response);
    }
}
