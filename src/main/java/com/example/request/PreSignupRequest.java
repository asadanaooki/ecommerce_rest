package com.example.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class PreSignupRequest {
    /*    // TODO:
     @Email意外に@Pattern付与する
    */
    @Email
    @NotBlank
    @Length(max = 255)
    private String email;
}
