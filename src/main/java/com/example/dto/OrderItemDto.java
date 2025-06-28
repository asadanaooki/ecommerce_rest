package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemDto {

    private String productId;
    
    private String productName;
    
    private int price;
    
    private int qty;
    
}
