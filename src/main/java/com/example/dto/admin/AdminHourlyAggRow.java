package com.example.dto.admin;

import lombok.Getter;

@Getter
public class AdminHourlyAggRow {

    private String bucketHour;
    
    private int revenue;
    
    private int orders;
}
