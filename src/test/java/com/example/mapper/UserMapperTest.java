package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.entity.User;
import com.example.request.ProfileUpdateRequest;
import com.example.util.RandomTokenUtil;

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

}
