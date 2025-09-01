package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemDto {

    private String productId;
    
    private String productName;
    
    private int unitPriceIncl;
    
    private int qty;
    
}
