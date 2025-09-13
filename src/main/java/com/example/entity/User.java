package com.example.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.enums.Role;

import lombok.Data;

@Data
public class User {
    private String userId;

    private String email;

    private String passwordHash;

    private String lastName;        // 姓
    private String firstName;       // 名
    private String lastNameKana;    // 姓カナ
    private String firstNameKana;   // 名カナ

    private String postalCode;      // 〒7桁（ハイフン無し）
    private String addressPrefCity; // 都道府県＋市区町村
    private String addressArea;     // 以降の住所（例：神南一丁目）
    private String addressBlock;    // 丁目・番地（例：1-19-11）
    private String addressBuilding; // 建物名・部屋番号（任意）

    private String phoneNumber;     // 0始まり 10-11 桁

    private LocalDate birthday;     // 生年月日
    private String     gender;      // M / F
    private String     nickname;
    
    private Role role;
    
    private String pendingEmail;
    private String emailTokenHash;
    private LocalDateTime pendingExpiresAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
