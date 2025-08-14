package com.example.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    
    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^[0-9A-Za-z]+$")
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^[0-9A-Za-z]+$")
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;
    
    @AssertTrue(message = "PASSWORDS_MATCH")
    public boolean isMatch() {
        return newPassword.equals(confirmPassword);
    }
}
