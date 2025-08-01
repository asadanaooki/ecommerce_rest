package com.example.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;

import lombok.Data;

@Data
public class AdminOrderDetailDto {

    private String orderId;
    
    private String orderNumber;
    
    private int totalPrice;
    
    private ShippingStatus shippingStatus;
    
    private PaymentStatus paymentStatus;
    
    private LocalDateTime createdAt;
    
    private List<AdminOrderDetailItemDto> items;
    
    
    // 購入者情報
    private String name;
    
    private String nameKana;
    
    private String email;
    
    private String postalCode;
    
    private String address;
    
    private String phoneNumber;
    
}
