package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductListDto {

    private List<AdminProductDto> items;
    
    private int total;
    
    private int pageSize;
}
