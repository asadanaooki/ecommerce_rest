package com.example.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCartRequest {
    // TODO:
    // productIdのバリデーションチェック？
    @NotBlank
    private String productId;

    @Min(1)
    @Max(20)
    private int qty = 1;
}
