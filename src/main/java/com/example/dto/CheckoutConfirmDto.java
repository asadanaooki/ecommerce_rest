package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutConfirmDto {

    private String username;
    
    private String postalCode;
    
    private String address;
    
    private CartDto cart;
    
  //  private int version;
    
//    private List<CartItemDto> items;
//    
//    private List<RemovedItemDto> removed;
//    
//    private int totalQty;
//    
//    private int totalPrice;
}
