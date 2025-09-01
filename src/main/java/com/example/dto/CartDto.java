package com.example.dto;

import java.util.Collections;
import java.util.List;

import com.example.util.OrderUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartDto {
    private List<CartItemDto> items = Collections.EMPTY_LIST;

    private int totalQty;
    
    private int totalPriceIncl;
    
    private int shippingFeeIncl;
    
    
    public CartDto(List<CartItemDto> items) {
        this.items = items;
        this.totalQty = OrderUtil.calculateTotalQty(items, CartItemDto::getQty);
        this.totalPriceIncl = OrderUtil.calculateTotalPriceIncl(items, CartItemDto::getSubtotalIncl);
        this.shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(totalPriceIncl);
    }
}
