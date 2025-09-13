package com.example.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.EmailChangeAlertOldContext;
import com.example.enums.MailTemplate.EmailChangeCompleteNewContext;
import com.example.enums.MailTemplate.ProfileChangedContext;
import com.example.error.BusinessException;
import com.example.mapper.UserMapper;
import com.example.request.EmailChangeRequest;
import com.example.request.PasswordChangeRequest;
import com.example.request.ProfileUpdateRequest;
import com.example.support.MailGateway;
import com.example.util.RandomTokenUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    /* TODO:
     * プロフィール編集画面開くときは、ステップアップ認証/認証フレッシュネス／max_age検討する。無印やニトリと同じ仕様
     * メール送信
        DB commit 後の失敗・HTTP 例外で片落ちの懸念。
        Transactional Outbox 方式（DB に mail_queue 行を insert → 非同期ジョブで送信＋リトライ）を推奨
        メール送信中のエラー再現してみる
     */

    private final UserMapper userMapper;

    private final PasswordEncoder encoder;

    private final MailGateway mailGateway;

    @Value("${settings.email-change.expire-minutes}")
    private long emailExpireMin;



    public void requestEmailChange(String userId, EmailChangeRequest req) {
        User u = userMapper.selectUserByPrimaryKey(userId);
        if (u.getEmail().equals(req.getNewEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMAIL_SAME");
        }

        if (userMapper.selectUserByEmail(req.getNewEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        String rawToken = RandomTokenUtil.generate();
        userMapper.saveEmailChangeRequest(userId, req.getNewEmail(), RandomTokenUtil.hash(rawToken),
                LocalDateTime.now().plusMinutes(emailExpireMin));

        String link = "http://localhost:8080/profile/email-change/complete?token=" + rawToken;
        mailGateway.send(
                MailTemplate.EMAIL_CHANGE_COMPLETE_NEW
                        .build(new EmailChangeCompleteNewContext(req.getNewEmail(), link, emailExpireMin)));
        mailGateway.send(
                MailTemplate.EMAIL_CHANGE_ALERT_OLD
                        .build(new EmailChangeAlertOldContext(u.getEmail(), LocalDateTime.now())));
    }

    public void completeEmailChange(String rawToken) {
        User user = userMapper.selectUserByTokenHash(RandomTokenUtil.hash(rawToken));
        if (user == null || user.getPendingExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        userMapper.confirmEmailChange(RandomTokenUtil.hash(rawToken));
    }

    public void changePassword(String userId, PasswordChangeRequest req) {
        User user = userMapper.selectUserByPrimaryKey(userId);
        if (!encoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD");
        }
        userMapper.updatePasswordByPrimaryKey(userId, encoder.encode(req.getNewPassword()));
        mailGateway.send(
                MailTemplate.PROFILE_CHANGED.build(new ProfileChangedContext(user.getEmail(), LocalDateTime.now())));
    }

    public void updateProfile(String userId, ProfileUpdateRequest req) {
        User user = userMapper.selectUserByPrimaryKey(userId);
        userMapper.updateProfile(userId, req);
        mailGateway.send(
                MailTemplate.PROFILE_CHANGED.build(new ProfileChangedContext(user.getEmail(), LocalDateTime.now())));
    }
}
