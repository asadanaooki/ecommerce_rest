package com.example.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.dto.ReviewDto;
import com.example.dto.ReviewPageDto;
import com.example.mapper.ReviewMapper;
import com.example.mapper.UserMapper;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    /* TODO:
    ・レビューを評価のみでよしとするか？
    ・退会ユーザーの内容考慮してない
    ・表示件数フロントで可変にする
    ・総件数毎回返すコスト　評価平均や総件数をキャッシュしたほうが良いかも
    ・不適切なレビュー削除対応
    ・返品やキャンセルで未購入となったら、レビューを削除するかどうするか検討
    ・現状レビュー即反映だが、反映するかの判断どうするか
    ・ユーザー自身のレビュー編集・削除をどうするか
    ・レビュー内容のサニタイズはフロントに任せる前提？
    ・レビュー一覧画面のフィルター、ソート
    */

    @Value("${settings.review.size}")
    private int pageSize;

    private final ReviewMapper reviewMapper;

    private final UserMapper userMapper;

    public ReviewPageDto fetchReviews(String productId, int page) {
        BigDecimal avg = reviewMapper.selectAvg(productId);
        int total = reviewMapper.countReviews(productId);
        List<ReviewDto> reviews = reviewMapper
                .selectReviews(productId, pageSize, PaginationUtil.calculateOffset(page, pageSize));

        return new ReviewPageDto(avg, total, pageSize, reviews);
    }

}
