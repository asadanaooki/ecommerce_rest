package com.example.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.json.annotation.TrimToNull;

import lombok.Data;

@Data
public class SubmitReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @TrimToNull
    @Size(max = 50)
    private String title;

    @TrimToNull
    @Size(max = 500)
    private String body;
}
