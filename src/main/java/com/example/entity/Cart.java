package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Cart {

    private String cartId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
