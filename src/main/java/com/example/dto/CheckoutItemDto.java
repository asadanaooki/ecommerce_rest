package com.example.dto;

import com.example.enums.SaleStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
public class CheckoutItemDto {
    // TODO:
    // SKU表示するほうがよい？

    /* ---------- 基本情報 ---------- */

    private String productId;

    private String productName;

    private int qty;

    /* ---------- 価格 ---------- */
    private int currentUnitPriceExcl;

    private int unitPriceExclAtAddToCart;

    private int unitPriceIncl;

    private int subtotalIncl;

    /* ---------- 販売状況 ---------- */
    private SaleStatus status;
    //    
    /* ---------- 在庫系 ---------- */
    private int available;
    
    /* ---------- 確認理由 ---------- */
    @Setter
    private DiffReason reason;
    
    
    public enum DiffReason{
        DISCONTINUED,
        OUT_OF_STOCK,
        LOW_STOCK,
        PRICE_CHANGED
    }

}
