package com.example.request.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class AdminInventoryMovementRequest {

    @NotNull
    @Positive
    private Integer qty;
    
    @NotNull
    @PositiveOrZero
    private Integer version;

}
