package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PreRegistration {

    private String token;
    
    private String email;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
