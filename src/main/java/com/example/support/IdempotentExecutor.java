package com.example.support;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.example.mapper.IdempotencyMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IdempotentExecutor {

    private final IdempotencyMapper idempotencyMapper;
    
    public void run(String key, Runnable action) {
        try {
            idempotencyMapper.insert(key);
        } catch (DuplicateKeyException e) {
            return;
        }
        action.run();
    }
}
