package com.example.feature.review;

import com.example.enums.review.ReviewStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReviewState {

    private final ReviewStatus status;
}
