package com.example.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mapper.CartMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartCleanupJob {
    /* TODO:
     * インデックス
     * 分割削除
     * 監視
     * バッチのテストどうやるか？今は簡単だから起動して簡易確認のみ
    */

    private final CartMapper cartMapper;
    
    @Scheduled(cron = "${settings.batch.cart.cron}")
    public void purgeExpiredCarts() {
        cartMapper.deleteExpiredCarts();
    }
}
