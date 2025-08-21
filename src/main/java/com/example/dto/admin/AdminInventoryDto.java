package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.StockStatus;

import lombok.Data;

@Data
public class AdminInventoryDto {

    private String productId;
    
    private String sku;
    
    private String productName;
    
    private int priceExcl;
    
    private int available;
    
    private StockStatus stockStatus;
    
    private LocalDateTime updatedAt;
    
}
