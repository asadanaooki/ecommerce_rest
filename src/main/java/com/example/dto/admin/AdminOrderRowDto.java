package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;

import lombok.Getter;

@Getter
public class AdminOrderRowDto {

    /* ---------- 識別情報 ---------- */
    private String orderId;
    private String orderNumber;

    /* ---------- 基本情報 ---------- */
    private LocalDateTime createdAt;
    private String name;

    /* ---------- 金額 ---------- */
    private int grandTotalIncl;

    /* ---------- ステータス ---------- */
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
