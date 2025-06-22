package com.example.service;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.PreRegistration;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.request.RegisterUserRequest;
import com.example.security.CustomUserDetails;
import com.example.util.JwtUtil;
import com.example.util.RandomTokenUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    /*    // TODO:
    ・メッセージをmessage.propertiesにまとめる
        有効期限切れ→「再送依頼ボタン」を同画面に置くと CS 工数減
        メール送信をトランザクション外にしたほうがよいか？メリットがわからん
        未使用トークンをバッチで削除
        自動ログインにする
    */
    private final AuthenticationManager manager;

    private final JwtUtil jwtUtil;

    private final UserMapper userMapper;

    private final JavaMailSender javaMailSender;

    private final PasswordEncoder encoder;

    @Value("${settings.auth.verification-ttl-min}")
    private long ttlMinutes;

    public AuthResult authenticate(String username, String password) {
        // TODO:
        // 認証処理をfilterに閉じ込めたほうがよいかも。今は簡単さを優先
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return new AuthResult(jwtUtil.issue(user.getUserId()), user.getUserId());
    }

    @Transactional
    public void sendRegistrationUrl(String email) throws MessagingException {
        if (userMapper.selectUserByEmail(email).isPresent()) {
            throw new BusinessException("signup.email.alreadyRegistered");
        }

        String token = RandomTokenUtil.generate();
        LocalDateTime exp = LocalDateTime.now().plusMinutes(ttlMinutes);
        PreRegistration pr = new PreRegistration();
        pr.setToken(token);
        pr.setEmail(email);
        pr.setExpiresAt(exp);

        userMapper.insertPreRegistration(pr);
        // TODO:
        // メール送信はローカルで仮実装
        // sendされるmsgの中身まで単体テストで見るべきか？setTextやsetSubjectなど
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, "UTF-8");
        String link = "http://localhost:8080/register/verify?token=" + token;

        h.setTo(email);
        h.setSubject("会員登録のご案内");
        h.setText("""
                以下のURLにアクセス頂き、お客様情報をご入力の上、会員登録をお願いいたします。

                %s

                %d分以内に会員登録が完了しない場合、このURLが無効となりますのでご注意ください。
                無効となった際は再度会員登録のページよりお進みください
                """.formatted(link, ttlMinutes));
        javaMailSender.send(msg);
    }

    public PreRegistration verify(String token) {
        PreRegistration pr = userMapper.selectPreRegistrationByPrimaryKey(token);
        if (pr == null || pr.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("signup.link.expired");
        }
        return pr;
    }

    @Transactional
    public void register(RegisterUserRequest req) {
        PreRegistration pr = userMapper.selectPreRegistrationByPrimaryKey(req.getToken());

        if (pr == null || !pr.getToken().equals(req.getToken())) {
            throw new BusinessException("signup.link.expired");
        }

        try {
            User user = toUserEntity(req, UUID.randomUUID().toString());

            userMapper.insertUser(user);
            userMapper.deletePreRegistrationByPrimaryKey(req.getToken());
        } catch (DuplicateKeyException e) {
            throw new BusinessException("signup.link.expired");
        }

        // TODO:
        // 歓迎メール送る
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

        /* --- 監査 --- */
        LocalDateTime now = LocalDateTime.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);

        return u;

    }
    
    public static record AuthResult(String jwt, String userId) {}
}
