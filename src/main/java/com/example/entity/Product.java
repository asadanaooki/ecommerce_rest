package com.example.entity;

import java.time.LocalDateTime;

import com.example.enums.SaleStatus;

import lombok.Data;

@Data
public class Product {
    private String productId;
    
    private int sku;
    
    private String productName;
    
    private String productDescription;
    
    private Integer price;
    
    private Integer stock;

    private SaleStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
