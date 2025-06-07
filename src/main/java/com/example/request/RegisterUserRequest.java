package com.example.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserRequest {
    @NotBlank(message = "error.unexpected")
    @Length(min = 22, max = 22, message = "error.unexpected")
    private String token;

    @Email(message = "register.email.invalid")
    @Length(max = 255, message = "register.email.invalid")
    @NotBlank(message = "register.email.invalid")
    private String email;

    /** パスワード */
    @NotBlank(message = "register.password.required")
    @Length(min = 8, max = 20, message = "register.password.length")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "register.password.pattern") // 英数のみ
    private String password;

    /** 氏名：姓 */
    @NotBlank(message = "register.last-name.required")
    @Length(max = 50, message = "register.last-name.length")
    private String lastName;

    /** 氏名：名 */
    @NotBlank(message = "register.first-name.required")
    @Length(max = 50, message = "register.first-name.length")
    private String firstName;

    /** フリガナ：姓（全角カタカナ＋長音） */
    @NotBlank(message = "register.last-name-kana.required")
    @Length(max = 50, message = "register.last-name-kana.length")
    @Pattern(regexp = "^[\\u30A0-\\u30FF]+$", message = "register.last-name-kana.pattern")
    private String lastNameKana;

    /** フリガナ：名（全角カタカナ＋長音） */
    @NotBlank(message = "register.first-name-kana.required")
    @Length(max = 50, message = "register.first-name-kana.length")
    @Pattern(regexp = "^[\\u30A0-\\u30FF]+$", message = "register.first-name-kana.pattern")
    private String firstNameKana;

    /** 郵便番号（ハイフン無し 7 桁） */
    @NotBlank(message = "register.post-code.required")
    @Pattern(regexp = "^[0-9]{7}$", message = "register.post-code.pattern")
    private String postCode;

    /** 住所：都道府県＋市区町村 */
    @NotBlank(message = "register.address-pref-city.required")
    @Length(max = 100, message = "register.address-pref-city.length")
    private String addressPrefCity;

    /** 住所：以降の住所（例：神南一丁目） */
    @NotBlank(message = "register.address-area.required")
    @Length(max = 100, message = "register.address-area.length")
    private String addressArea;

    /** 住所：丁目・番地（例：1-19-11） */
    @NotBlank(message = "register.address-block.required")
    @Length(max = 100, message = "register.address-block.length")
    private String addressBlock;

    /** 住所：建物名・部屋番号（任意） */
    @Length(max = 100, message = "register.address-building.length")
    private String addressBuilding;

    /** 電話番号（ハイフン無し／0 始まり 10-11 桁） */
    @NotBlank(message = "register.phone-number.required")
    @Pattern(regexp = "^0\\d{9,10}$", message = "register.phone-number.pattern")
    private String phoneNumber;

    /** 生年月日（未来日は不可） */
    @NotNull(message = "register.birthday.required")
    @Past(message = "register.birthday.past")
    private LocalDate birthday;

    /** 性別：M=男性 / F=女性 */
    @NotBlank(message = "register.gender.required")
    @Pattern(regexp = "^[MF]$", message = "register.gender.pattern")
    private String gender;
}
