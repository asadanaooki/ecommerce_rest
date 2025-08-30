package com.example.entity;

import java.time.LocalDateTime;

import com.example.enums.order.RejectReason;
import com.example.enums.review.ReviewStatus;

import lombok.Data;

@Data
public class Review {
    
    private String productId;

    private String userId;
    
    private int rating;
    
    private String title;
    
    private String reviewText;
    
    private ReviewStatus status;
    
    private RejectReason rejectReason;
    
    private String rejectNote;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
