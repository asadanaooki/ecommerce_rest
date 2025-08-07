package com.example.dto.admin;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdminDailyAggRow {

    private LocalDate day;
    
    private int revenue;
    
    private int orders;
}
