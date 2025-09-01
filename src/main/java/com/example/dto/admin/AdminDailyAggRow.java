package com.example.dto.admin;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class AdminDailyAggRow {

    private LocalDate day;
    
    private int revenue;
    
    private int orders;
}
