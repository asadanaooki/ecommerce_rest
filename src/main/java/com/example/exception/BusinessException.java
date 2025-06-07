package com.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 業務例外
 */
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final String code;

    private final String messageKey;

    private Object data;

    public BusinessException(String messageKey) {
        this.code = "";
        this.messageKey = messageKey;
    }
    
    public BusinessException(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
