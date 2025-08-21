package com.example.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class OrderHistoryDto {

    private String orderId;
    
    private String orderNumber;

    private LocalDate orderedAt;

    int totalPriceIncl;

    private String name;

    private String postalCode;

    private String address;

    List<OrderItemDto> items;
}
