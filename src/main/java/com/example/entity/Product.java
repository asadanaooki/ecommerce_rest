package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Product {
    private String productId;
    
    private int sku;
    
    private String productName;
    
    private String productDescription;
    
    private Integer price;
    
    private Integer stock;
    
    // TODO:
    // ENUM型(SaleStatus)に変えたい
    // 数値⇔ENUMのハンドラ必要かも
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
