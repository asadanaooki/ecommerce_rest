package com.example.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FavoritePageDto {
    
    private final List<FavoriteRow> items;
    
    private final int pageSize;
    
    private final int totalItems;
    
    @AllArgsConstructor
    @Data
    public static class FavoriteRow{
        private final String productId;
        
        private final String productName;
        
        private final int price;
        
        private final String saleStatus;
    }

}
