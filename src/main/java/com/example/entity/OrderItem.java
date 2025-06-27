package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 注文明細（order_item テーブル）
 */
@Data
public class OrderItem {

    /** order_item.order_id */
    private String orderId;

    /** order_item.product_id */
    private String productId;

    /** 数量（order_item.qty） */
    private int qty;

    /** 単価（税込 or 税抜は業務ルールに合わせて）（order_item.price） */
    private int price;

    /** 小計（price × qty）（order_item.subtotal） */
    private int subtotal;

    /** 作成日時（order_item.created_at） */
    private LocalDateTime createdAt;

    /** 更新日時（order_item.updated_at） */
    private LocalDateTime updatedAt;
}
