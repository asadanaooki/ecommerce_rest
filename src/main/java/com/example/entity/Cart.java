package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Cart {

    private String cartId;

    private String userId;

    // private int version;
    
    private int ttlDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
