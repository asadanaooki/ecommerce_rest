package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse {
    private final String resultCode;

    private final String messageKey;

    Object data;

    public ApiResponse(String messageKey) {
        this.resultCode = "";
        this.messageKey = messageKey;
    }
    
    public ApiResponse(String code, String messageCode) {
        this.resultCode = code;
        this.messageKey = messageCode;
    }
}
