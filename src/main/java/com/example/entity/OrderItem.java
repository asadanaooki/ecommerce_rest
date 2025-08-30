package com.example.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 注文明細（order_item テーブル）
 */
@Data
public class OrderItem {
    // TODO:
    // 複数税率→軽減税率など 運用面や会計連携などで必要な時に税抜き価格や税額カラム追加を検討

    /** order_item.order_id */
    private String orderId;

    /** order_item.product_id */
    private String productId;
    
    private String productName;

    /** 数量（order_item.qty） */
    private int qty;

    private int unitPriceIncl;

    private int subtotalIncl;

    /** 作成日時（order_item.created_at） */
    private LocalDateTime createdAt;

    /** 更新日時（order_item.updated_at） */
    private LocalDateTime updatedAt;
}
