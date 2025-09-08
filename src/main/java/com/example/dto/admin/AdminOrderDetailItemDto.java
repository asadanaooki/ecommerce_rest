package com.example.dto.admin;

import lombok.Data;

@Data
public class AdminOrderDetailItemDto {

    private String productId;
    
    private String sku;
    
    private String productName;
    
    private int unitPriceIncl;
    
    private int qty;
    
    private int subtotalIncl;

}
