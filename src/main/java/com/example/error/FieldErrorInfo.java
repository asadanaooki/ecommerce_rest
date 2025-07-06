package com.example.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldErrorInfo {

    private String field;
    
    // TODO:
    // fail-fastにしたら何のバリデーションエラーか分かるよ名称にしたほうがよい
    private String errorCode;
    
}
