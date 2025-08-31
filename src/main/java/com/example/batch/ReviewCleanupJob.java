package com.example.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewCleanupJob {
    // TODO:
    //  否認時刻を追加するべきか？
    //  インデックス
    //  分割削除
    //  監視

    private final ReviewMapper reviewMapper;
    
    @Scheduled(cron = "${settings.batch.review.cron}")
    public void purgeRejectedReviews() {
        reviewMapper.deleteRejected();
    }
}
