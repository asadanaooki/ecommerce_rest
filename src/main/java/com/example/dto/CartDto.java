package com.example.dto;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartDto {
    private List<CartItemDto> items = Collections.EMPTY_LIST;

    private int totalQty;
    
    private int totalPriceIncl;
    
    public CartDto(List<CartItemDto> items) {
        this.items = items;
        this.totalQty = items.stream()
                .mapToInt(CartItemDto::getQty)
                .sum();
        
        this.totalPriceIncl = items.stream()
                .mapToInt(CartItemDto::getSubtotalIncl)
                .sum();
    }
}
