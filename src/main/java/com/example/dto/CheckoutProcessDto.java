package com.example.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutProcessDto {

    private String fullName;

    private String postalCode;

    private String fullAddress;

    private List<CheckoutItemDto> items;

    private int totalQty;

    private int totalPriceIncl;

}
