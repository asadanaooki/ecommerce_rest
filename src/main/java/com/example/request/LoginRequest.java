package com.example.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class LoginRequest {
    /*    // TODO:
     usernameに@Email意外に@Pattern付与する
    アカウントロック
        一定回数連続失敗でロック → 管理画面 or メール解除
    監査ログ
        「失敗→成功」の連続など不審パターンを可視化
    */
    @Email
    @NotBlank
    @Length(max = 255)
    private String username;

    @NotBlank
    @Length(min = 8, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String password;
}
