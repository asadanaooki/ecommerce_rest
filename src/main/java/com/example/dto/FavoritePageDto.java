package com.example.dto;

import java.util.List;

import com.example.enums.SaleStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class FavoritePageDto {

    private final List<FavoriteRow> items;

    private final int pageSize;

    private final int totalItems;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class FavoriteRow {
        private String productId;

        private String productName;

        private int priceIncl;

        private SaleStatus status;
    }

}
