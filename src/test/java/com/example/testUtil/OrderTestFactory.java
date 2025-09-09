package com.example.testUtil;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import com.example.entity.Order;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.util.OrderUtil;

public class OrderTestFactory {

    public static Order buildOrder(Consumer<Order> customizer) {
        Order o = new Order();
        o.setOrderId(UUID.randomUUID().toString());
        o.setUserId("550e8400-e29b-41d4-a716-446655440000");
        o.setName("山田 太郎");
        o.setPostalCode("1500041");
        o.setAddress("test");
        o.setTotalQty(2);
        o.setItemsSubtotalIncl(4000);
        o.setShippingFeeIncl(500);
        o.setCodFeeIncl(OrderUtil.obtainCodFeeIncl());
        o.setOrderStatus(OrderStatus.OPEN);
        o.setShippingStatus(ShippingStatus.UNSHIPPED);
        o.setPaymentStatus(PaymentStatus.UNPAID);
        o.setCreatedAt(LocalDateTime.of(2020, 1, 1, 10, 3, 4));
        o.setUpdatedAt(LocalDateTime.of(2020, 1, 1, 10, 3, 4));
        customizer.accept(o);
        return o;
    }
}
