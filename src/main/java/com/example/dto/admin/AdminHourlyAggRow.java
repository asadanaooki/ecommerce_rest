package com.example.dto.admin;

import lombok.Data;

@Data
public class AdminHourlyAggRow {

    private String bucketHour;
    
    private int revenue;
    
    private int orders;
}
