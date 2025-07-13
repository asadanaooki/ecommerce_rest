package com.example.enums;

import lombok.Getter;

@Getter
public enum SaleStatus {
    // TODO:
    // Enumのフォールバックを共通化する？
    
    UNPUBLISHED("0"),
    PUBLISHED("1");
    
    private final String code;
    
    private SaleStatus(String code) {
        this.code = code;
    }
    
    public static SaleStatus fromCode(String code) {
        for (SaleStatus s : values()) {
            if (s.code.equals(code)) {
                return s;
            }
        }
        return null;
    }
}
