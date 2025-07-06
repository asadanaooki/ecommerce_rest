package com.example.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 業務例外
 */
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private HttpStatus httpStatus;
    
    private final String errorCode;

    private Object data;
    
    public BusinessException(HttpStatus status) {
        this.httpStatus = status;
        this.errorCode = "";
    }

    public BusinessException(HttpStatus status, String errorCode) {
        this.httpStatus = status;
        this.errorCode = errorCode;
    }
    
    public BusinessException(HttpStatus status, Object body) {
        this.httpStatus = status;
        this.errorCode = "";
        data = body;
    }
}
