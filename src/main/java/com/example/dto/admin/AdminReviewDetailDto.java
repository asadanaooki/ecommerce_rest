package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.review.ReviewStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminReviewDetailDto {

    // --- 識別子 ---
    private String productId;
    private String userId;

    // --- レビュー内容 ---
    private int rating;
    private String title;
    private String reviewText;
    private LocalDateTime createdAt;

    // --- 商品情報 ---
    private String productName;
    private String sku;
    private String productThumbUrl;

    // --- 投稿者情報 ---
    private String nickname;

    // --- 管理用メタ情報 ---
    private ReviewStatus status;
}
