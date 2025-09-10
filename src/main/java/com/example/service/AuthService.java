package com.example.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.PasswordResetToken;
import com.example.entity.PreRegistration;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.EmailChangeAlertOldContext;
import com.example.enums.MailTemplate.EmailChangeCompleteNewContext;
import com.example.enums.MailTemplate.PasswordResetContext;
import com.example.enums.MailTemplate.ProfileChangedContext;
import com.example.enums.MailTemplate.RegistrationContext;
import com.example.enums.MailTemplate.WelcomeContext;
import com.example.error.BusinessException;
import com.example.mapper.UserMapper;
import com.example.request.EmailChangeRequest;
import com.example.request.PasswordChangeRequest;
import com.example.request.PasswordResetMailRequest;
import com.example.request.PasswordResetUpdateRequest;
import com.example.request.ProfileUpdateRequest;
import com.example.request.RegisterUserRequest;
import com.example.security.CustomUserDetails;
import com.example.support.MailGateway;
import com.example.util.JwtUtil;
import com.example.util.RandomTokenUtil;
import com.example.util.UserUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    /* TODO:
     * 有効期限切れ→「再送依頼ボタン」を同画面に置くと CS 工数減
     * 未使用トークンをバッチで削除
     * パスワードリセットで、列挙攻撃対策としてパスワード送信時にトークンチェックする？
     * ユーザーが登録アドレス忘れた場合どうするか？
     * プロフィール編集画面開くときは、ステップアップ認証/認証フレッシュネス／max_age検討する。無印やニトリと同じ仕様
     * authenticate→認証処理をfilterに閉じ込めたほうがよいかも。今は簡単さを優先
     * パスワード再設定、ユーザー登録案内メールの再送ポリシー
     * メール送信
        DB commit 後の失敗・HTTP 例外で片落ちの懸念。
        Transactional Outbox 方式（DB に mail_queue 行を insert → 非同期ジョブで送信＋リトライ）を推奨
        メール送信中のエラー再現してみる
     */

    private final AuthenticationManager manager;

    private final JwtUtil jwtUtil;

    private final UserMapper userMapper;

    private final PasswordEncoder encoder;

    private final MailGateway mailGateway;

    @Value("${settings.auth.verification-ttl-min}")
    private long ttlMinutes;

    @Value("${settings.reset.expire-minutes}")
    private long expireMin;

    @Value("${settings.email-change.expire-minutes}")
    private long emailExpireMin;

    public AuthResult authenticate(String username, String password) {
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String authority = user.getAuthorities().iterator().next().getAuthority();
        String role = authority.substring("ROLE_".length());

        return new AuthResult(jwtUtil.issue(user.getUserId(), role), user.getUserId());
    }

    @Transactional
    public void sendRegistrationUrl(String email) throws MessagingException {
        if (userMapper.selectUserByEmail(email).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT);
        }

        String token = RandomTokenUtil.generate();
        LocalDateTime exp = LocalDateTime.now().plusMinutes(ttlMinutes);
        PreRegistration pr = new PreRegistration();
        pr.setToken(RandomTokenUtil.hash(token));
        pr.setEmail(email);
        pr.setExpiresAt(exp);

        userMapper.insertPreRegistration(pr);

        String link = "http://localhost:8080/register/verify?token=" + token;
        mailGateway.send(MailTemplate.REGISTRATION.build(new RegistrationContext(email, link, ttlMinutes)));
    }

    public PreRegistration verify(String token) {
        PreRegistration pr = userMapper.selectPreRegistrationByPrimaryKey(
                RandomTokenUtil.hash(token));
        if (pr == null || pr.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.NOT_FOUND);
        }
        return pr;
    }

    @Transactional
    public String register(RegisterUserRequest req) {
        PreRegistration pr = userMapper.selectPreRegistrationByPrimaryKey(
                RandomTokenUtil.hash(req.getToken()));

        if (pr == null || !pr.getEmail().equals(req.getEmail())) {
            throw new BusinessException(HttpStatus.NOT_FOUND);
        }

        User user = toUserEntity(req, UUID.randomUUID().toString());

        userMapper.insertUser(user);
        userMapper.deletePreRegistrationByPrimaryKey( RandomTokenUtil.hash(req.getToken()));

        mailGateway.send(MailTemplate.WELCOME.build(
                new WelcomeContext(user.getEmail(),
                        UserUtil.buildFullName(user),
                        "http://localhost:8080/product")));

        return jwtUtil.issue(user.getUserId(), "USER");
    }

    @Transactional
    public void sendPasswordRestMail(PasswordResetMailRequest req) {
        Optional<User> op = userMapper.selectUserByEmail(req.getEmail());
        if (op.isEmpty() || !op.get().getBirthday().equals(req.getBirthday())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        User user = op.get();
        String rawToken = RandomTokenUtil.generate();

        userMapper.insertPasswordResetToken(new PasswordResetToken() {
            {
                setTokenHash(RandomTokenUtil.hash(rawToken));
                setEmail(req.getEmail());
                setExpiresAt(LocalDateTime.now().plusMinutes(expireMin));
            }
        });

        String link = "http://localhost:8080/password-reset/form?token=" + rawToken;
        mailGateway.send(MailTemplate.PASSWORD_RESET.build(
                new PasswordResetContext(req.getEmail(), link, expireMin)));
    }

    @Transactional
    public void resetPassword(PasswordResetUpdateRequest req) {
        String hashed = RandomTokenUtil.hash(req.getToken());
        PasswordResetToken tk = userMapper
                .selectPasswordResetTokenByPrimaryKey(hashed);
        if (tk == null ||
                LocalDateTime.now().isAfter(tk.getExpiresAt()) ||
                !tk.getEmail().equals(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        userMapper.updatePasswordByEmail(req.getEmail(), encoder.encode(req.getNewPassword()));
        userMapper.deletePasswordResetToken(hashed);
    }

    public void requestEmailChange(String userId, EmailChangeRequest req) {
        User u = userMapper.selectUserByPrimaryKey(userId);
        if (u.getEmail().equals(req.getNewEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMAIL_SAME");
        }

        if (userMapper.selectUserByEmail(req.getNewEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        String token = RandomTokenUtil.generate();
        userMapper.saveEmailChangeRequest(userId, req.getNewEmail(), RandomTokenUtil.hash(token),
                LocalDateTime.now().plusMinutes(emailExpireMin));

        String link = "http://localhost:8080/profile/email-change/complete?token=" + token;
        mailGateway.send(
                MailTemplate.EMAIL_CHANGE_COMPLETE_NEW
                        .build(new EmailChangeCompleteNewContext(req.getNewEmail(), link, emailExpireMin)));
        mailGateway.send(
                MailTemplate.EMAIL_CHANGE_ALERT_OLD
                        .build(new EmailChangeAlertOldContext(u.getEmail(), LocalDateTime.now())));
    }

    public void completeEmailChange(String token) {
        User user = userMapper.selectUserByToken(RandomTokenUtil.hash(token));
        if (user == null || user.getPendingExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        userMapper.confirmEmailChange(RandomTokenUtil.hash(token));
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

    private User toUserEntity(RegisterUserRequest r, String userId) {
        User u = new User();
        u.setUserId(userId);
        u.setEmail(r.getEmail());
        u.setPasswordHash(encoder.encode(r.getPassword()));

        /* --- 氏名 --- */
        u.setLastName(r.getLastName());
        u.setFirstName(r.getFirstName());
        u.setLastNameKana(r.getLastNameKana());
        u.setFirstNameKana(r.getFirstNameKana());

        /* --- 住所 --- */
        u.setPostalCode(r.getPostCode());
        u.setAddressPrefCity(r.getAddressPrefCity());
        u.setAddressArea(r.getAddressArea());
        u.setAddressBlock(r.getAddressBlock());
        u.setAddressBuilding(r.getAddressBuilding());

        /* --- 連絡先・その他 --- */
        u.setPhoneNumber(r.getPhoneNumber());
        u.setBirthday(r.getBirthday());
        u.setGender(r.getGender());

        return u;

    }

    public record AuthResult(String jwt, String userId) {
    }
}
