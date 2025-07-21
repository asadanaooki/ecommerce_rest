package com.example.entity;

import java.time.LocalDateTime;

import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;

import lombok.Data;

/**
 * 注文ヘッダ（order テーブル）
 */
@Data
public class Order {

    /** orders.order_id */
    private String orderId;
    
    private int orderNumber;

    /** orders.user_id */
    private String userId;

    /** お届け先氏名（orders.username） */
    private String name;

    /** 郵便番号（orders.postal_code） */
    private String postalCode;

    /** 住所 1 行まとめ（orders.address） */
    private String address;

    /** 合計数量（orders.total_qty） */
    private int totalQty;

    /** 合計金額（税込）（orders.total_price） */
    private int totalPrice;
    
    private ShippingStatus shippingStatus;
    
    private PaymentStatus paymentStatus;
    
    /** 作成日時（orders.created_at） */
    private LocalDateTime createdAt;

    /** 更新日時（orders.updated_at） */
    private LocalDateTime updatedAt;
}
