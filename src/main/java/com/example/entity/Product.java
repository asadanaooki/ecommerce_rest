package com.example.entity;

import java.time.LocalDateTime;

import com.example.enums.SaleStatus;

import lombok.Data;

@Data
public class Product {
    // TODO:
    // 税込み価格はViewで管理。インデックスを貼る場合は別の方法を検討
    
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
