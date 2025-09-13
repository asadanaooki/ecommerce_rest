package com.example.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.error.FieldErrorInfo;
import com.example.request.EmailChangeRequest;
import com.example.request.PasswordChangeRequest;
import com.example.request.ProfileUpdateRequest;
import com.example.service.AccountService;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/account")
public class AccountController {
    /* TODO:
     * プロフィール表示(GET)のAPIがない
    */

    private final AccountService accountService;

    // プロフィール
//    @GetMapping("/profile")
//    public ResponseEntity<T> showProfile(@AuthenticationPrincipal String userId) {
//        
//    }
    
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal String userId,
            @RequestBody @Valid ProfileUpdateRequest req) {
        accountService.updateProfile(userId, req);

        return ResponseEntity.ok().build();
    }
    
    // メール変更
    @PostMapping("/email-change-request")
    public ResponseEntity<List<FieldErrorInfo>> reqeustEmailChange(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid EmailChangeRequest req) {
        accountService.requestEmailChange(userId, req);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/email-change")
    public ResponseEntity<Void> completeEmailChange(
            @RequestParam @NotBlank @Size(min = 22, max = 22) String rawToken) {
        accountService.completeEmailChange(rawToken);

        return ResponseEntity.ok().build();
    }

    // パスワード変更
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal String userId,
            @RequestBody @Valid PasswordChangeRequest req) {
        accountService.changePassword(userId, req);
        return ResponseEntity.ok().build();
    }



}
