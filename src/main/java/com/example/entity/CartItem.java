package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CartItem {

    private String cartId;
    
    private String productId;
    
    private int qty;
    
    private int priceIncTax;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
