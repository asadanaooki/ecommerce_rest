package com.example.request;

import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import com.example.json.annotation.NormalizeEmail;
import com.example.validation.constraint.EmailFormat;

import lombok.Data;

@Data
public class EmailChangeRequest {

    @NormalizeEmail
    @EmailFormat
    @NotBlank
    @Length(max = 254)
    private String newEmail;

    @NormalizeEmail
    @NotBlank
    private String confirmEmail;

    public boolean isMatch() {
        return newEmail.equals(confirmEmail);
    }
}
