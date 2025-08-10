package com.example.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.dto.ReviewDto;
import com.example.dto.ReviewPageDto;
import com.example.entity.Review;
import com.example.error.BusinessException;
import com.example.mapper.ReviewMapper;
import com.example.mapper.UserMapper;
import com.example.request.ReviewPostRequest;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    /* TODO:
    ・レビューテーブル→タイトル追加する
    ・退会ユーザーの内容考慮してない
    ・表示件数フロントで可変にする
    ・総件数毎回返すコスト　評価平均や総件数をキャッシュしたほうが良いかも
    ・不適切なレビュー削除対応
    ・返品やキャンセルで未購入となったら、レビューを削除するかどうするか検討
    ・現状レビュー即反映だが、反映するかの判断どうするか
    ・ユーザー自身のレビュー編集・削除をどうするか
    ・レビュー内容のサニタイズはフロントに任せる前提？
    */
    
    @Value("${settings.review.size}")
    private int pageSize;
    
    private final ReviewMapper reviewMapper;
    
    private final UserMapper userMapper;
    
    public ReviewPageDto fetchReviews(String productId, int page) {
        int total = reviewMapper.countReviews(productId);
        List<ReviewDto> reviews = reviewMapper
                .selectReviews(productId, pageSize, PaginationUtil.calculateOffset(page, pageSize));
        
        BigDecimal avg = reviews.isEmpty() ? BigDecimal.ZERO
                : reviews.stream()
                .map(ReviewDto::getRating)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviews.size()),1, RoundingMode.HALF_UP);
        
        return new ReviewPageDto(avg, total, pageSize, reviews);
    }
    
    public void postReview(String productId, String userId, ReviewPostRequest req) {
        if (userMapper.selectUserByPrimaryKey(userId).getNickname() == null) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (!reviewMapper.hasPurchased(userId, productId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN);
        }
        
        Review r = new Review();
        r.setProductId(productId);
        r.setUserId(userId);
        r.setRating(req.getRating());
        r.setReviewText(req.getReviewText());
        
        reviewMapper.insertReview(r);
    }

}
