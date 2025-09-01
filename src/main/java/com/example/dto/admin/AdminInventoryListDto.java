package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminInventoryListDto {

    private List<AdminInventoryRowDto> content;
    
    private int  total;
    
    private int size;
}
