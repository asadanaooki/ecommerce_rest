package com.example.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldErrorInfo {

    private String field;
    
    private String errorCode;
    
}
