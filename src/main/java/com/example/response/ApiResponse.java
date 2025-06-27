package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse {
    private final String errorCode;

    Object data;
    
    public ApiResponse(String errorCode) {
        this.errorCode = errorCode;
    }
}
