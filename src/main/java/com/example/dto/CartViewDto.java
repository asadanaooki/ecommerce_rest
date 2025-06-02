package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class CartViewDto {
    private final List<CartItemDto> items;

    private int totalQty;

    private int totalAmount;

    public CartViewDto(List<CartItemDto> items) {
        this.items = items;
        
        this.totalQty = items.stream()
                .filter(ci -> ci.isOnSale())
                .mapToInt(ci -> ci.getQty())
                .sum();

        this.totalAmount = items.stream()
                .mapToInt(ci -> ci.getSubtotal())
                .sum();
    }
}
