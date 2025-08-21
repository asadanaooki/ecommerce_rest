package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CartItemDto {
    // TODO:
    // SKU表示するほうがよい？

    /* ---------- 基本情報 ---------- */

    private String productId;

    private String productName;

    private int qty;

    /* ---------- 価格 ---------- */
    @JsonIgnore
    private int priceExcl;

    @JsonProperty("price")
    private Integer priceInc;

    private int subtotal;

}
