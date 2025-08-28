package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;

import lombok.Data;

@Data
public class AdminOrderDto {

    /* ---------- 識別情報 ---------- */
    private String orderId;
    private String orderNumber;

    /* ---------- 基本情報 ---------- */
    private LocalDateTime createdAt;
    private String name;

    /* ---------- 金額 ---------- */
    private int totalPriceIncl;

    /* ---------- ステータス ---------- */
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
