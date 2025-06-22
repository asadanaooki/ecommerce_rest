package com.example.dto;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private String cartId;

    private List<CartItemDto> items = Collections.EMPTY_LIST;

    private int totalQty;
    
    private int totalPrice;
}
