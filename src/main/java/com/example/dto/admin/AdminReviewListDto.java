package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminReviewListDto {

    private List<AdminReviewRowDto> items;
}
