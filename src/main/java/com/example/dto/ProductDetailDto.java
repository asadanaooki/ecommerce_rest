package com.example.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductDetailDto {
    private String productId;
    
    private String productName;
    
    private String productDescription;
    
    private int priceIncl;
    
    private boolean outOfStock;
    
    private BigDecimal ratingAvg;
    
    private int reviewCount;
    
    private boolean isFav;
}
