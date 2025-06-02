package com.example.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private final String productId;

    private final String productName;

    private final int priceInc; // 最新 税込単価（円）

    private final int prevPriceInc; // 前回表示 税込単価（円）

    private final int qty; // 数量 (1–20)

    private final boolean onSale; // true = 販売中

    private final boolean priceChanged; // 単価が変わったか

    private final int subtotal;

    public CartItemDto(String productId,
            String productName,
            int priceInc,
            int prevPriceInc,
            boolean onSale,
            int qty) {
        this.productId = productId;
        this.productName = productName;
        this.onSale = onSale;
        if (onSale) {
            this.priceInc = priceInc;
            this.prevPriceInc = prevPriceInc;
            this.qty = qty;
            this.subtotal = priceInc * qty;
            this.priceChanged = priceInc != prevPriceInc;
        } else {
            this.priceInc = 0; // 販売停止なら必ず 0
            this.prevPriceInc = 0; // UI で「旧価格」を出したいなら残す
            this.qty = 1; // 仕様上 1 固定なら 1
            this.subtotal = 0;
            this.priceChanged = false; // 旧価格と違えば true
        }
    }

}
