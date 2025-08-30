package com.example.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminReviewDetailDto;
import com.example.dto.admin.AdminReviewListDto;
import com.example.enums.review.ReviewStatus;
import com.example.request.review.RejectReviewRequest;
import com.example.service.ReviewCommandService;
import com.example.service.admin.AdminReviewService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    private final ReviewCommandService reviewCommandService;

    @GetMapping
    public ResponseEntity<AdminReviewListDto> showList(
            @RequestParam(required = false) ReviewStatus status) {
        AdminReviewListDto dto = adminReviewService.getReviewSummaries(status);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{productId}/{userId}")
    public ResponseEntity<AdminReviewDetailDto> showDetail(
            @PathVariable @HexUuid @NotBlank String productId,
            @PathVariable @HexUuid @NotBlank String userId) {
        AdminReviewDetailDto dto = adminReviewService.getDetail(productId, userId);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{productId}/{userId}/approve")
    public void approve(
            @PathVariable @HexUuid @NotBlank String productId,
            @PathVariable @HexUuid @NotBlank String userId) {
        reviewCommandService.approve(productId, userId);
    }

    @PostMapping("/{productId}/{userId}/reject")
    public void reject(
            @PathVariable @HexUuid @NotBlank String productId,
            @PathVariable @HexUuid @NotBlank String userId,
            @RequestBody @Valid RejectReviewRequest req) {
        reviewCommandService.reject(productId, userId, req);
    }

}
