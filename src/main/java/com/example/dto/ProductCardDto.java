package com.example.dto;

import lombok.Data;

@Data
public class ProductCardDto {
    private String productId;
    
    private String productName;
    
    private int priceIncl;
    
    private boolean outOfStock;
    
    private boolean isFav;
}
