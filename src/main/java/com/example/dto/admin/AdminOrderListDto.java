package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminOrderListDto {

    private List<AdminOrderRowDto> content;
    
    private int  total;
    
    private int size;
}
