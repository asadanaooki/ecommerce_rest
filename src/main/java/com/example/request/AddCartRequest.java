package com.example.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCartRequest {

    @Min(1)
    @Max(20)
    // 商品詳細画面から追加時は数量選択可。お気に入り画面からは固定値１
    private int qty = 1;
}
