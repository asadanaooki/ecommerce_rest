package com.example.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor         // ← フレームワーク用デフォルトコンストラクタ
@AllArgsConstructor
public class ProfileUpdateRequest {
    /* TODO:
     * Mapperの引数までもっていくのやめたほうがよい？
     */

    // ④ 氏名
    @NotBlank @Size(max = 50) private String lastName;
    @NotBlank @Size(max = 50) private String firstName;

    // ⑤ フリガナ（全角カタカナ＋長音）
    @NotBlank @Size(max = 50)
    @Pattern(regexp = "^[\\u30A0-\\u30FF]+$")
    private String lastNameKana;

    @NotBlank @Size(max = 50)
    @Pattern(regexp = "^[\\u30A0-\\u30FF]+$")
    private String firstNameKana;

    // ⑥ 郵便番号（7 桁）
    @NotBlank
    @Pattern(regexp = "^[0-9]{7}$")
    private String postalCode;

    // ⑦ 住所
    @NotBlank @Size(max = 100) private String addressPrefCity;
    @NotBlank @Size(max = 100) private String addressArea;
    @NotBlank @Size(max = 100) private String addressBlock;
    @Size(min = 1, max = 100) private String addressBuilding; // 任意

    // ⑧ 電話番号
    @NotBlank
    @Length(min = 11, max = 11)
    @Pattern(regexp = "^[0-9]+$")
    private String phoneNumber;

    // ⑨ 生年月日（未来日不可）
    @NotNull
    @Past
    private LocalDate birthday;

    // ⑩ 性別（M/F）
    @NotBlank
    @Pattern(regexp = "^[MF]$")
    private String gender;
    
    @Size(min = 1, max = 15)
    private String nickname;
}
