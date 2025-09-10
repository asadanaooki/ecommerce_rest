package com.example.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.mapper.IdempotencyMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IdempotencyCleanupJob {

    private final IdempotencyMapper idempotencyMapper;
    
    @Scheduled(cron = "${settings.batch.cron-common}")
    public void purgeExpiredIdempotencies() {
        idempotencyMapper.deleteExpired();
    }
}
