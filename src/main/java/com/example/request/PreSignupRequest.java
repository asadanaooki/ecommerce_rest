package com.example.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import com.example.json.annotation.NormalizeEmail;

import lombok.Data;

@Data
public class PreSignupRequest {
    /* TODO:
     * 国際化ドメイン対応検討
     */
    
    @NormalizeEmail
    @Email
    @NotBlank
    @Length(max = 254)
    private String email;
}
