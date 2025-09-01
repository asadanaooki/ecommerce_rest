package com.example.dto;

import java.util.List;

import com.example.util.OrderUtil;

import lombok.Data;

@Data
public class CheckoutProcessDto {

    private String fullName;

    private String postalCode;

    private String fullAddress;

    private List<CheckoutItemDto> items;

    private int totalQty;

    private int totalPriceIncl;

    private int shippingFeeIncl;

    public CheckoutProcessDto(
            String fullName,
            String postalCode,
            String fullAddress,
            List<CheckoutItemDto> items) {
        this.fullName = fullName;
        this.postalCode = postalCode;
        this.fullAddress = fullAddress;

        this.items = items;
        this.totalQty = OrderUtil.calculateTotalQty(items, CheckoutItemDto::getQty);
        this.totalPriceIncl = OrderUtil.calculateTotalPriceIncl(items, CheckoutItemDto::getSubtotalIncl);
        this.shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(totalPriceIncl);
    }

}
