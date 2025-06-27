package com.example.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.entity.PreRegistration;
import com.example.entity.User;

@Mapper
public interface UserMapper {
    User selectUserByPrimaryKey(String userId);

    Optional<User> selectUserByEmail(String email);
    
    PreRegistration selectPreRegistrationByPrimaryKey(String token);
    
    int insertPreRegistration(PreRegistration preg);
    
    int deletePreRegistrationByPrimaryKey(String token);
    
    int insertUser(User user);

}
