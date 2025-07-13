package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse {
    private final String resultCode;

    Object data;
    
    public ApiResponse(String code) {
        this.resultCode = code;
    }
}
