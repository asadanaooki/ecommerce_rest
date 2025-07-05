package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PasswordResetToken {

    private String tokenHash;
    
    private String email;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
