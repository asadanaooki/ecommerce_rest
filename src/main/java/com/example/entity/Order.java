package com.example.entity;

import java.time.LocalDateTime;

import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * 注文ヘッダ（order テーブル）
 */
@Data
public class Order {
    // TODO:
    // 税額、税抜き合計保留→会計処理や売上分析などで必要になったら検討

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

    private int totalQty;

    /** 商品小計（税込）: orders.items_subtotal_incl */
    private int itemsSubtotalIncl;

    /** 発送送料（税込）: orders.shipping_fee_incl */
    private int shippingFeeIncl;

    /** 総額（税込）: orders.grand_total_incl (GENERATED ALWAYS STORED) */
    @Setter(AccessLevel.NONE)
    private int grandTotalIncl;

    private OrderStatus orderStatus;

    private ShippingStatus shippingStatus;

    private PaymentStatus paymentStatus;

    /** 作成日時（orders.created_at） */
    private LocalDateTime createdAt;

    /** 更新日時（orders.updated_at） */
    private LocalDateTime updatedAt;
}
