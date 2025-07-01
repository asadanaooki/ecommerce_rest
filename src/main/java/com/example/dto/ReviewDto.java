package com.example.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewDto {

    private String nickname;
    
    private LocalDate createdDate;
    
    private int rating;
    
    private String reviewText;
}
