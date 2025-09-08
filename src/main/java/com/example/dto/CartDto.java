package com.example.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.example.util.OrderUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartDto {
    private List<CartItemDto> items = Collections.EMPTY_LIST;

    private int totalQty;

    private int itemsSubtotalIncl;

    private int shippingFeeIncl;
    
    @JsonInclude(Include.NON_NULL)
    private Integer codFeeIncl;

    private int grandTotalIncl;

    public CartDto(List<CartItemDto> items, Integer codFeeIncl) {
        this.items = items;
        this.totalQty = OrderUtil.sumBy(items, CartItemDto::getQty);
        this.itemsSubtotalIncl = OrderUtil.sumBy(items, CartItemDto::getSubtotalIncl);
        this.shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(itemsSubtotalIncl);
        this.codFeeIncl = codFeeIncl;
        this.grandTotalIncl = OrderUtil
                .calculateGrandTotalIncl(itemsSubtotalIncl,
                        shippingFeeIncl,
                        Objects.requireNonNullElse(codFeeIncl, 0)
                        );
    }
}
