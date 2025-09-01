package com.example.dto;

import lombok.Getter;

@Getter
public class CartItemDto {
    // TODO:
    // SKU表示するほうがよい？

    private String productId;

    private String productName;

    private int qty;

    private int unitPriceIncl;

    private int subtotalIncl;

}
