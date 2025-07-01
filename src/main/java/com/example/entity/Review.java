package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Review {
    
    private String productId;

    private String userId;
    
    private int rating;
    
    private String reviewText;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
