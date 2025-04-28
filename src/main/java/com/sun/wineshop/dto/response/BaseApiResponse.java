package com.sun.wineshop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BaseApiResponse <T> {
    private int code = 200;
    private T data;
    private String message;
}
