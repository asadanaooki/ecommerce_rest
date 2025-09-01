package com.example.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDto {

   private List<ProductCardDto> products;
    
   private int totalPage;
   
   private List<Integer> pageNumbers;
}
