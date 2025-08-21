package com.example.dto;

import java.util.List;

import com.example.enums.SaleStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class FavoritePageDto {

    private final List<FavoriteRow> items;

    private final int pageSize;

    private final int totalItems;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class FavoriteRow {
        private String productId;

        private String productName;

        private int priceIncl;

        private SaleStatus status;
    }

}
