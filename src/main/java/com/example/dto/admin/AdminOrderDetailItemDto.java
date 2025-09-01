package com.example.dto.admin;

import lombok.Getter;

@Getter
public class AdminOrderDetailItemDto {

    private String productId;
    
    private String productName;
    
    private int unitPriceIncl;
    
    private String qty;
    
    private int subtotalIncl;

}
