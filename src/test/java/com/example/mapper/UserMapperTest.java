package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.entity.PasswordResetToken;
import com.example.entity.PreRegistration;
import com.example.entity.User;
import com.example.request.ProfileUpdateRequest;
import com.example.testUtil.FlywayResetExtension;
import com.example.util.RandomTokenUtil;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserMapperTest {

    @Autowired
    UserMapper userMapper;

    String userId = "550e8400-e29b-41d4-a716-446655440000";

    String pendEmail = "test@example.com";

    String token = RandomTokenUtil.hash("a".repeat(22));

    LocalDateTime expire = LocalDateTime.of(2025, 7, 5, 10, 41, 2);

    @Test
    void saveEmailChangeRequest() {
        User expected = userMapper.selectUserByPrimaryKey(userId);
        expected.setPendingEmail(pendEmail);
        expected.setEmailToken(token);
        expected.setPendingExpiresAt(expire);

        int rows = userMapper.saveEmailChangeRequest(userId, pendEmail, token, expire);
        User user = userMapper.selectUserByPrimaryKey(userId);

        assertThat(rows).isOne();
        assertThat(user).usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(expected);

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void confirmEmailChange() {
        userMapper.saveEmailChangeRequest(userId, pendEmail, token, expire);
        User expected = userMapper.selectUserByPrimaryKey(userId);
        expected.setEmail(pendEmail);
        expected.setEmailToken(null);
        expected.setPendingEmail(null);
        expected.setPendingExpiresAt(null);

        int rows = userMapper.confirmEmailChange(token);
        User user = userMapper.selectUserByPrimaryKey(userId);

        assertThat(rows).isOne();
        assertThat(user).usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(expected);

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateProfile() {
        ProfileUpdateRequest req = new ProfileUpdateRequest(
                "佐藤", "次郎",
                "サトウ", "ジロウ",
                "1600022",
                "東京都新宿区",
                "新宿三丁目",
                "2-5-10",
                "グランドタワー8F 801号室",
                "09087654321",
                LocalDate.of(1985, 12, 24),
                "F",
                "satojiro");

        int rows = userMapper.updateProfile(userId, req);

        User user = userMapper.selectUserByPrimaryKey(userId);
        assertThat(rows).isOne();
        assertThat(user.getLastName()).isEqualTo("佐藤");
        assertThat(user.getFirstName()).isEqualTo("次郎");
        assertThat(user.getLastNameKana()).isEqualTo("サトウ");
        assertThat(user.getFirstNameKana()).isEqualTo("ジロウ");
        assertThat(user.getPostalCode()).isEqualTo("1600022");
        assertThat(user.getAddressPrefCity()).isEqualTo("東京都新宿区");
        assertThat(user.getAddressArea()).isEqualTo("新宿三丁目");
        assertThat(user.getAddressBlock()).isEqualTo("2-5-10");
        assertThat(user.getAddressBuilding()).isEqualTo("グランドタワー8F 801号室");
        assertThat(user.getPhoneNumber()).isEqualTo("09087654321");
        assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1985, 12, 24));
        assertThat(user.getGender()).isEqualTo("F");
        assertThat(user.getNickname()).isEqualTo("satojiro");
    }

    @Test
    void selectUserByEmail() {
        User user = userMapper.selectUserByEmail("sample@example.com").orElse(null);
        
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo("sample@example.com");
        assertThat(user.getFirstName()).isEqualTo("太郎");
        assertThat(user.getLastName()).isEqualTo("山田");
    }

    @Test
    void selectUserByEmail_returnsNullForNonExistent() {
        User user = userMapper.selectUserByEmail("nonexistent@example.com").orElse(null);
        assertThat(user).isNull();
    }

    @Test
    void insertAndSelectPasswordResetToken() {
        String testEmail = "sample@example.com";
        String tokenHash = RandomTokenUtil.hash("reset".repeat(4));
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(tokenHash);
        resetToken.setEmail(testEmail);
        resetToken.setExpiresAt(expiresAt);
        int rows = userMapper.insertPasswordResetToken(resetToken);
        assertThat(rows).isOne();
        
        PasswordResetToken token = userMapper.selectPasswordResetTokenByPrimaryKey(tokenHash);
        assertThat(token).isNotNull();
        assertThat(token.getTokenHash()).isEqualTo(tokenHash);
        assertThat(token.getEmail()).isEqualTo(testEmail);
    }

    @Test
    void deletePasswordResetToken() {
        String testEmail = "sample@example.com";
        String tokenHash = RandomTokenUtil.hash("delete".repeat(4));
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(tokenHash);
        resetToken.setEmail(testEmail);
        resetToken.setExpiresAt(expiresAt);
        userMapper.insertPasswordResetToken(resetToken);
        
        int rows = userMapper.deletePasswordResetToken(tokenHash);
        assertThat(rows).isOne();
        
        PasswordResetToken token = userMapper.selectPasswordResetTokenByPrimaryKey(tokenHash);
        assertThat(token).isNull();
    }

    @Test
    void updatePasswordByPrimaryKey() {
        String newPasswordHash = "$2a$10$newhashedpassword";
        
        int rows = userMapper.updatePasswordByPrimaryKey(userId, newPasswordHash);
        assertThat(rows).isOne();
        
        User user = userMapper.selectUserByPrimaryKey(userId);
        assertThat(user.getPasswordHash()).isEqualTo(newPasswordHash);
    }

    @Test
    void updatePasswordByEmail() {
        String newPasswordHash = "$2a$10$anotherhashedpassword";
        String email = "sample@example.com";
        
        int rows = userMapper.updatePasswordByEmail(email, newPasswordHash);
        assertThat(rows).isOne();
        
        User user = userMapper.selectUserByEmail(email).orElse(null);
        assertThat(user.getPasswordHash()).isEqualTo(newPasswordHash);
    }

    @Test
    void selectUserByToken() {
        String emailToken = RandomTokenUtil.hash("emailtoken".repeat(2));
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
        
        userMapper.saveEmailChangeRequest(userId, "newemail@example.com", emailToken, expiresAt);
        
        User user = userMapper.selectUserByToken(emailToken);
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmailToken()).isEqualTo(emailToken);
    }

    @Test
    void insertAndSelectPreRegistration() {
        String preRegToken = "PREREG123456789";
        String preRegEmail = "newuser@example.com";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        
        PreRegistration newPreReg = new PreRegistration();
        newPreReg.setToken(preRegToken);
        newPreReg.setEmail(preRegEmail);
        newPreReg.setExpiresAt(expiresAt);
        int rows = userMapper.insertPreRegistration(newPreReg);
        assertThat(rows).isOne();
        
        PreRegistration preReg = userMapper.selectPreRegistrationByPrimaryKey(preRegToken);
        assertThat(preReg).isNotNull();
        assertThat(preReg.getToken()).isEqualTo(preRegToken);
        assertThat(preReg.getEmail()).isEqualTo(preRegEmail);
    }

    @Test
    void deletePreRegistrationByPrimaryKey() {
        String preRegToken = "DELTOKEN123456";
        String preRegEmail = "deleteuser@example.com";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        
        PreRegistration delPreReg = new PreRegistration();
        delPreReg.setToken(preRegToken);
        delPreReg.setEmail(preRegEmail);
        delPreReg.setExpiresAt(expiresAt);
        userMapper.insertPreRegistration(delPreReg);
        
        int rows = userMapper.deletePreRegistrationByPrimaryKey(preRegToken);
        assertThat(rows).isOne();
        
        PreRegistration preReg = userMapper.selectPreRegistrationByPrimaryKey(preRegToken);
        assertThat(preReg).isNull();
    }

    @Test
    void insertUser() {
        String newUserId = UUID.randomUUID().toString();
        User newUser = new User();
        newUser.setUserId(newUserId);
        newUser.setEmail("inserttest" + System.currentTimeMillis() + "@example.com");
        newUser.setPasswordHash("$2a$10$testhash");
        newUser.setLastName("テスト");
        newUser.setFirstName("太郎");
        newUser.setLastNameKana("テスト");
        newUser.setFirstNameKana("タロウ");
        newUser.setPostalCode("1234567");
        newUser.setAddressPrefCity("東京都千代田区");
        newUser.setAddressArea("丸の内");
        newUser.setAddressBlock("1-1-1");
        newUser.setAddressBuilding("ビル5F");
        newUser.setPhoneNumber("0312345678");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));
        newUser.setGender("M");
        newUser.setNickname("testnick");
        
        int rows = userMapper.insertUser(newUser);
        assertThat(rows).isOne();
        
        User insertedUser = userMapper.selectUserByPrimaryKey(newUserId);
        assertThat(insertedUser).isNotNull();
        assertThat(insertedUser.getEmail()).contains("inserttest");
        assertThat(insertedUser.getFirstName()).isEqualTo("太郎");
        assertThat(insertedUser.getNickname()).isEqualTo("testnick");
    }

}
