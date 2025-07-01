package com.example.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewPageDto {

    private BigDecimal averageRating;
    
    private int totalCount;
    
    private int pageSize;
    
    private List<ReviewDto> items;
}
