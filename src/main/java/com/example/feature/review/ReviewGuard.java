package com.example.feature.review;

import org.springframework.stereotype.Component;

import com.example.entity.Review;
import com.example.enums.review.ReviewEvent;
import com.example.enums.review.ReviewStatus;
import com.example.mapper.ReviewMapper;
import com.example.util.GuardUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewGuard {
    /* TODO:
     * 冪等性考慮→現状、エラー
     * nextメソッド
         Approve:否認後の承認ケース検討
         Reject:承認後の否認ケース検討
    */

    private final ReviewMapper reviewMapper;
    
    public ReviewState next(ReviewState cur, ReviewEvent ev) {
        if (cur.getStatus() == ReviewStatus.APPROVED) {
            throw new IllegalStateException("Terminal state");
        }
        
        return switch (ev) {
        case SUBMIT -> {
            if (cur.getStatus() == ReviewStatus.PENDING) {
                // 申請中の内容更新（再投稿ではなく“上書き”）
                yield new ReviewState(ReviewStatus.PENDING);
            } else if (cur.getStatus() == ReviewStatus.REJECTED) {
                // 否認後の再投稿
                yield new ReviewState(ReviewStatus.PENDING);
            }
            throw new IllegalStateException("Submit not allowed");
        }
        case APPROVE -> {
            if (cur.getStatus() == ReviewStatus.PENDING) {
                yield new ReviewState(ReviewStatus.APPROVED);
            }
            throw new IllegalStateException("Approve not allowed");
        }
        case REJECT -> {
            if (cur.getStatus() == ReviewStatus.PENDING) {
                yield new ReviewState(ReviewStatus.REJECTED);
            }
            throw new IllegalStateException("Reject not allowed");
        }
        };
    }
    
    public Review require(String productId, String userId) {
        return GuardUtil.ensureFound(reviewMapper.selectByPrimaryKey(productId, userId),
                "REVIEW_NOT_FOUND");
    }
}
