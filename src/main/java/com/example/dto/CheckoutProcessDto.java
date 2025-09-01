package com.example.dto;

import java.util.List;

import com.example.util.OrderUtil;

import lombok.Getter;

@Getter
public class CheckoutProcessDto {

    private String fullName;

    private String postalCode;

    private String fullAddress;

    private List<CheckoutItemDto> items;

    private int totalQty;

    private int itemsSubtotalIncl;

    private int shippingFeeIncl;

    private int grandTotalIncl;

    
    public CheckoutProcessDto(
            String fullName,
            String postalCode,
            String fullAddress,
            List<CheckoutItemDto> items) {
        this.fullName = fullName;
        this.postalCode = postalCode;
        this.fullAddress = fullAddress;

        this.items = items;
        this.totalQty = OrderUtil.sumBy(items, CheckoutItemDto::getQty);
        this.itemsSubtotalIncl = OrderUtil.sumBy(items, CheckoutItemDto::getSubtotalIncl);
        this.shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(itemsSubtotalIncl);
        this.grandTotalIncl = OrderUtil
                .calculateGrandTotalIncl(itemsSubtotalIncl, shippingFeeIncl);
    }

}
