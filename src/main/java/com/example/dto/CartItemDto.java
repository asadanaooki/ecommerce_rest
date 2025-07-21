package com.example.dto;

import com.example.enums.SaleStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CartItemDto {
    // TODO:
    // 色々アノテーションついててごちゃごちゃしてる。分けた方が良いかも
    // SKU表示するほうがよい？

    /* ---------- 基本情報 ---------- */

    private String productId;

    private String productName;

    private int qty;

    /* ---------- 価格 ---------- */
    @JsonIgnore
    private int priceEx;

    @JsonIgnore
    private int priceAtCartAddition;

    @JsonProperty("price")
    private Integer priceInc;

    private int subtotal;

    /* ---------- 販売状況 ---------- */
    @JsonIgnore
    private SaleStatus status;
    //    
    /* ---------- 在庫系 ---------- */
    @JsonIgnore
    private Integer stock;
    
    /* ---------- 確認理由 ---------- */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DiffReason reason;
    
    public enum DiffReason{
        DISCONTINUED,
        OUT_OF_STOCK,
        LOW_STOCK,
        PRICE_CHANGED
    }

}
