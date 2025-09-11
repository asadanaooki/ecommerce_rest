package com.example.feature.user;

import org.springframework.stereotype.Component;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.util.GuardUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserGuard {

    private final UserMapper userMapper;
    
    public User require(String userId) {
        return GuardUtil.ensureFound(userMapper.selectUserByPrimaryKey(userId),
                "USER_NOT_FOUND");
    }
}
