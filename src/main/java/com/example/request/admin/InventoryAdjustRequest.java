package com.example.request.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class InventoryAdjustRequest {

    @NotNull
    @PositiveOrZero
    private Integer stock;

    @NotNull
    @PositiveOrZero
    private Integer reserved;

    @AssertTrue
    private boolean isValidRelation() {
        if (stock == null || reserved == null) {
            return true;
        }
        return stock >= reserved;
    }

}
