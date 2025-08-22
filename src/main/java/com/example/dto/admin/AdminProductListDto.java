package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductListDto {

    private List<AdminProductDto> items;
    
    private int total;
    
    private int pageSize;
}
