package com.example.dto;

import java.util.Collections;
import java.util.List;

import com.example.util.OrderUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartDto {
    private List<CartItemDto> items = Collections.EMPTY_LIST;

    private int totalQty;

    private int itemsSubtotalIncl;

    private int shippingFeeIncl;

    private int grandTotalIncl;

    public CartDto(List<CartItemDto> items) {
        this.items = items;
        this.totalQty = OrderUtil.sumBy(items, CartItemDto::getQty);
        this.itemsSubtotalIncl = OrderUtil.sumBy(items, CartItemDto::getSubtotalIncl);
        this.shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(itemsSubtotalIncl);
        this.grandTotalIncl = OrderUtil
                .calculateGrandTotalIncl(itemsSubtotalIncl, shippingFeeIncl);
    }
}
