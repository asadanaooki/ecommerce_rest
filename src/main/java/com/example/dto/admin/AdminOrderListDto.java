package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminOrderListDto {

    private List<AdminOrderDto> content;
    
    private int  total;
    
    private int size;
}
