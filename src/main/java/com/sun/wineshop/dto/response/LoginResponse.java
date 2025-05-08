package com.sun.wineshop.dto.response;

public record LoginResponse(boolean isSuccess, String token) {
}
