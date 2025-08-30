package com.example.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminReviewListDto {

    // --- レビュー内容 ---
    private int rating;
    private String reviewText;
    private LocalDateTime updatedAt;

    // --- 商品情報 ---
    private String productName;
    private String sku;

    // --- 投稿者情報 ---
    private String nickname;

    // --- 識別子（管理用） ---
    private String productId;
    private String userId;
}
