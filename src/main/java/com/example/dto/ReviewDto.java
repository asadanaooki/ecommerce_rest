package com.example.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDto {

    private String nickname;
    
    private LocalDate createdDate;
    
    private int rating;
    
    private String title;
    
    private String reviewText;
}
