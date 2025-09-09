package com.example.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminFileDto {

    private String fileName;
    
    private byte[] bytes;

}
