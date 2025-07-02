package com.example.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class PasswordResetUpdateRequest {

    @NotBlank
    @Length(min = 8, max = 20)
    @Pattern(regexp = "^[0-9A-Za-z]+$")
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;
    
    @AssertTrue
    public boolean isMatch() {
        if (newPassword == null || confirmPassword == null) {
            return false;
        }
        return newPassword.equals(confirmPassword);
    }
}
