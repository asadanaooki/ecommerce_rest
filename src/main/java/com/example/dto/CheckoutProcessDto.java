package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class CheckoutProcessDto {

    private String fullName;

    private String postalCode;

    private String fullAddress;

    private List<CheckoutItemDto> items;

    private int totalQty;

    private int totalPriceIncl;
    
    
    public CheckoutProcessDto(
            String fullName,
            String postalCode,
            String fullAddress,
            List<CheckoutItemDto> items) {
        this.fullName = fullName;
        this.postalCode = postalCode;
        this.fullAddress = fullAddress;
        
        this.items = items;
        this.totalQty = this.items.stream()
                .mapToInt(CheckoutItemDto::getQty)
                .sum();
        this.totalPriceIncl = this.items.stream()
                .mapToInt(CheckoutItemDto::getSubtotalIncl)
                .sum();
    }

}
