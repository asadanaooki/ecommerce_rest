package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemovedItemDto {
    
    @JsonIgnore
    private String productId;

    private String productName;
    
    private Reason reason;
    
    public enum Reason{
        DISCONTINUED,
        OUT_OF_STOCK
    }
}
