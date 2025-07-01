package com.example.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ReviewPostRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String reviewText;
}
