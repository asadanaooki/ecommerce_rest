package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CartItemDto {

    private String productId;

    private String productName;

    private int qty;

    @JsonIgnore
    private int priceEx;
    
    @JsonProperty("price")
    private Integer priceInc;
    
    private Integer subtotal;

    private boolean priceChanged;
}
