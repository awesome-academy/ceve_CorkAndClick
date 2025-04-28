package com.sun.wineshop.exception;

import com.sun.wineshop.dto.response.BaseApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        BaseApiResponse<Void> baseApiResponse = new BaseApiResponse<>();
        baseApiResponse.setCode(errorCode.getCode());
        baseApiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(baseApiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseApiResponse<?>> handleRuntimeException(RuntimeException exception) {
        BaseApiResponse<?> baseApiResponse = new BaseApiResponse<>();
        baseApiResponse.setCode(ErrorCode.UNCATEGORIZED.getCode());
        baseApiResponse.setMessage(ErrorCode.UNCATEGORIZED.getMessage());
        return ResponseEntity.badRequest().body(baseApiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String enumKey = (fieldError != null) ? fieldError.getDefaultMessage() : "UNCATEGORIZED";
        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (Exception exception) {
            errorCode = ErrorCode.INVALID_ERROR_KEY;
        }
        BaseApiResponse<?> baseApiResponse = new BaseApiResponse<>();
        baseApiResponse.setCode(errorCode.getCode());
        baseApiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(baseApiResponse);
    }
}
