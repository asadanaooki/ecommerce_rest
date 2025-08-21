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
    
    private Integer priceExcl;
    
    private Integer stock;
    
    private Integer reserved;

    private SaleStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
