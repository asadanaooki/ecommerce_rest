package com.example.entity.view;

import java.time.LocalDateTime;

import com.example.enums.SaleStatus;

import lombok.Data;

@Data
public class ProductCoreView {

private String productId;
    
    private String sku;
    
    private String productName;
    
    private String productDescription;
    
    private Integer price;
    
    private Integer stock;
    
    private Integer reserved;
    
    private Integer available;

    private SaleStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
