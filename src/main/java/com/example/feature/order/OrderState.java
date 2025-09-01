package com.example.feature.order;

import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderState {

    private final OrderStatus order;
    
    private final ShippingStatus shipping;
    
    private final PaymentStatus payment;
}
