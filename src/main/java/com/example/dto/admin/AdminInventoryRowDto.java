package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.StockStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AdminInventoryRowDto {

    private String productId;
    
    private String sku;
    
    private String productName;
    
    private int priceExcl;
    
    private int available;
    
    @Setter
    private StockStatus stockStatus;
    
    private LocalDateTime updatedAt;
    
}
