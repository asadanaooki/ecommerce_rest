package com.example.entity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class CartItem {

    private String cartId;
    
    private String productId;
    
    private int qty;
    
    private int unitPriceExcl;
    
    @Setter(AccessLevel.NONE)
    private int unitPriceIncl;

    @Setter(AccessLevel.NONE)
    private int subtotalIncl;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
