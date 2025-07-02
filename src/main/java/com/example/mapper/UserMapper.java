package com.example.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.entity.PasswordResetToken;
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
    
    int insertPasswordResetToken(PasswordResetToken token);
    
    PasswordResetToken selectPasswordResetTokenByPrimaryKey(String hash);
    
    int deletePasswordResetToken(String hash);
    
    // TODO:
    // プロフィール編集クエリと統合する？
    int updatePassword(String userId, String pw);

}
