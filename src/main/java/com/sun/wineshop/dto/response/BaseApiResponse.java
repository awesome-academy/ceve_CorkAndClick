package com.sun.wineshop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseApiResponse<T> {
    private int code;
    private T data;
    private String message;

    public BaseApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseApiResponse(int code, T data) {
        this.code = code;
        this.data = data;
    }
}
