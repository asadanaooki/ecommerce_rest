package com.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.ReviewPageDto;
import com.example.request.review.SubmitReviewRequest;
import com.example.service.ReviewCommandService;
import com.example.service.ReviewService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    private final ReviewCommandService reviewCommandService;

    @GetMapping("/{productId}")
    public ReviewPageDto showReviews(@PathVariable @HexUuid @NotBlank String productId,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return reviewService.fetchReviews(productId, page);
    }

    @PostMapping("/{productId}")
    public void postReview(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid SubmitReviewRequest req) {
        reviewCommandService.submit(productId, userId, req);
    }
}
