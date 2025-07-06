package com.example.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class EmailChangeRequest {

    @Email
    @NotBlank
    @Length(max = 255)
    private String newEmail;

    @NotBlank
    private String confirmEmail;

    public boolean isMatch() {
        // TODO:
        // fail-fastにしたら以下の判定不要
        if (newEmail == null || confirmEmail == null) {
            return false;
        }
        return newEmail.equals(confirmEmail);
    }
}
