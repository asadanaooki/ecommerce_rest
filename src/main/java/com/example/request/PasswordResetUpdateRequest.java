package com.example.request;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.json.annotation.NormalizeEmail;
import com.example.validation.constraint.EmailFormat;

import lombok.Data;

@GroupSequence({PasswordResetUpdateRequest.class, MatchCheck.class})
@Data
public class PasswordResetUpdateRequest {

    @NotBlank
    @Size(min = 22, max = 22)
    private String token;

    @NormalizeEmail
    @EmailFormat
    @NotBlank
    @Size(max = 254)
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^[0-9A-Za-z]+$")
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "PASSWORDS_MATCH", groups = MatchCheck.class)
    public boolean isMatch() {
        return newPassword.equals(confirmPassword);
    }
}

interface MatchCheck {
}
