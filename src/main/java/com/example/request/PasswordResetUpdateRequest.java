package com.example.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class PasswordResetUpdateRequest {
    
    @NotBlank
    @Size(min = 22, max = 22)
    private String token;
    
    @Email
    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
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
