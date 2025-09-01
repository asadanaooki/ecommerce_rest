package com.example.dto;

import lombok.Getter;

@Getter
public class ProductCardDto {
    private String productId;
    
    private String productName;
    
    private int priceIncl;
    
    private boolean outOfStock;
    
    private boolean isFav;
}
