package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Cart {

    private String cartId;
    
    private String userId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
