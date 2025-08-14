package com.example.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import com.example.json.annotation.NormalizeEmail;
import com.example.validation.constraint.EmailFormat;

import lombok.Data;

@Data
public class PasswordResetMailRequest {
    // TODO:
    // userテーブルで生年月日必須にしてるが、任意にして別の追加情報も選択できるようにする？

    @NormalizeEmail
    @EmailFormat
    @NotBlank
    private String email;
    
    @NotNull
    @Past
    private LocalDate birthday;
}
