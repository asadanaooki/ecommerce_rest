package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IdempotencyMapper {
    
    int insert(String idempotencyKey);

    int deleteExpired();
}
