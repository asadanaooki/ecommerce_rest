package com.example.dto;

import lombok.Data;

@Data
public class CartItemDto {
    // TODO:
    // SKU表示するほうがよい？

    private String productId;

    private String productName;

    private int qty;

    private int unitPriceIncl;

    private Integer subtotalIncl;

}
