package com.example.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import lombok.Data;

@Data
public class PasswordResetMailRequest {
    // TODO:
    // 生年月日以外に追加情報選択できるようにする？

    @Email
    @NotBlank
    private String email;
    
    @NotNull
    @Past
    private LocalDate birthday;
}
