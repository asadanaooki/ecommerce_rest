package com.example.feature.order;

import org.springframework.stereotype.Component;

import com.example.enums.order.OrderEvent;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;

@Component
public class OrderTransitionGuard {
    // TODO:
    // 単調増加のテスト検討
    // 冪等性考慮→現状、エラー
    // 許可ホワイトリスト＋全列挙ループのデータ駆動テスト検討

    public OrderState next(OrderState cur, OrderEvent ev) {
        if (cur.getOrder() == OrderStatus.CANCELED
                || cur.getOrder() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Terminal state");
        }
        return switch (ev) {
        case REQUEST_CANCEL: {
            if (cur.getOrder() == OrderStatus.OPEN
                    && cur.getShipping() == ShippingStatus.UNSHIPPED
                    && cur.getPayment() == PaymentStatus.UNPAID) {
                yield new OrderState(OrderStatus.CANCEL_REQUESTED,
                        cur.getShipping(),
                        cur.getPayment());
            }
            throw new IllegalStateException("Cancel request not allowed");
        }
        case APPROVE_CANCEL: {
            if (cur.getOrder() == OrderStatus.CANCEL_REQUESTED
                    && cur.getShipping() == ShippingStatus.UNSHIPPED
                    && cur.getPayment() == PaymentStatus.UNPAID) {
                yield new OrderState(OrderStatus.CANCELED,
                        cur.getShipping(),
                        cur.getPayment());
            }
            throw new IllegalStateException("Approve not allowed");
        }
        case SHIP: {
            if ((cur.getOrder() == OrderStatus.OPEN
                    || cur.getOrder() == OrderStatus.CANCEL_REQUESTED)
                    && cur.getShipping() == ShippingStatus.UNSHIPPED
                    && cur.getPayment() == PaymentStatus.UNPAID) {
                // 出荷時点で CANCEL_REQUESTED は自動解消して OPEN に統一している想定
                yield new OrderState(OrderStatus.OPEN,
                        ShippingStatus.SHIPPED, cur.getPayment());
            }
            throw new IllegalStateException("Ship not allowed");
        }
        case DELIVERED: {
            if (cur.getOrder() == OrderStatus.OPEN
                    && cur.getShipping() == ShippingStatus.SHIPPED
                    && cur.getPayment() == PaymentStatus.UNPAID) {

                yield new OrderState(OrderStatus.COMPLETED,
                        ShippingStatus.DELIVERED, PaymentStatus.PAID);
            }
            throw new IllegalStateException("Delivered not allowed");
        }
        };
    }
}
