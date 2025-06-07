package com.example.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddCartRequest {
    // TODO:
    // productIdやdisplayedPriceのバリデーションチェック？
    
    private String productId;

    @Min(1)
    @Max(20)
    private int qty;
    
    private int priceIncTax;
}
