package com.example.dto.admin;

import lombok.Data;

@Data
public class AdminOrderDetailItemDto {

    private String productId;
    
    private String productName;
    
    private int unitPriceIncl;
    
    private String qty;
    
    private int subtotalIncl;

}
