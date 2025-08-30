package com.example.mapper.param;

import com.example.enums.order.RejectReason;
import com.example.enums.review.ReviewEvent;
import com.example.enums.review.ReviewStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewUpdateParam {
    // --- 識別子（複合キー） ---
    private String productId;
    private String userId;

    // --- 遷移情報 ---
    private ReviewEvent event; // SUBMIT / APPROVE / REJECT
    private ReviewStatus fromStatus; // WHERE 条件（現在ステータス）
    private ReviewStatus toStatus; // SET 値（次ステータス）

    // --- 更新ペイロード（イベント別に利用） ---
    // SUBMIT 用
    private Integer rating;
    private String title;
    private String body;

    // REJECT 用
    private RejectReason rejectReason;
    private String rejectNote; // null
}
