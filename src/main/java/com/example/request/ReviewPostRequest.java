package com.example.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ReviewPostRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(min = 1, max = 50)
    @Pattern(regexp = "(?s)(?U).*\\S.*")
    private String title;

    @Size(min = 1, max = 500)
    @Pattern(regexp = "(?s)(?U).*\\S.*")
    private String reviewText;
}
