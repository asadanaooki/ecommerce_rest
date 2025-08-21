package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;

import lombok.Data;

@Data
public class AdminOrderDto {

    private String orderId;
    
    private String orderNumber;
    
    private int totalPriceIncl;
    
    private String name;
    
    private ShippingStatus shippingStatus;
    
    private PaymentStatus paymentStatus;
    
    private LocalDateTime createdAt;
}
