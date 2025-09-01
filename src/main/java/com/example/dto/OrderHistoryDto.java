package com.example.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.enums.order.OrderStatus;

import lombok.Data;

@Data
public class OrderHistoryDto {

    /* ---------- 識別情報 ---------- */
    private String orderId;
    private String orderNumber;
    private LocalDate orderedAt;

    /* ---------- 顧客情報 ---------- */
    private String name;
    private String postalCode;
    private String address;

    /* ---------- 注文内容 ---------- */
    private List<OrderItemDto> items;

    /* ---------- 金額・状態 ---------- */
    private int itemsSubtotalIncl;
    private int shippingFeeIncl;
    private int grandTotalIncl;
    private OrderStatus orderStatus;
}
