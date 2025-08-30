package com.example.dto.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminReviewRowDto {

    // --- レビュー内容 ---
    private int rating;
    
    @JsonIgnore
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
    
    
    public String getExcerpt() {
        if (reviewText == null) {
            return null;
        }
        String normalized = reviewText.replaceAll("\\s+　", " ");
        
        int max = 100;
        if (normalized.length() <= max) {
            return normalized;
        }
        return normalized.substring(0, max) + "…";
    }
}
