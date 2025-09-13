package com.example.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.entity.PasswordResetToken;
import com.example.entity.PreRegistration;
import com.example.entity.User;
import com.example.request.ProfileUpdateRequest;

@Mapper
public interface UserMapper {
    /* TODO:
     * Mapper分けた方がよいかも、可読性的に
    */
    
    User selectUserByPrimaryKey(String userId);

    Optional<User> selectUserByEmail(String email);
    
    PreRegistration selectPreRegistrationByPrimaryKey(String tokenHash);
    
    int insertPreRegistration(PreRegistration preg);
    
    int deletePreRegistrationByPrimaryKey(String tokenHash);
    
    int insertUser(User user);
    
    int insertPasswordResetToken(PasswordResetToken passwordResetToken);
    
    PasswordResetToken selectPasswordResetTokenByPrimaryKey(String hash);
    
    int deletePasswordResetToken(String tokenHash);
    
    int updatePasswordByPrimaryKey(String userId, String pw);
    
    int updatePasswordByEmail(String email, String pw);
    
    int saveEmailChangeRequest(String userId, String  newEmail, String tokenHash, LocalDateTime expiresAt);
    
    User selectUserByTokenHash(String tokenHash);
    
    int confirmEmailChange(String tokenHash);

    int updateProfile(String userId, ProfileUpdateRequest req);
}
