package com.example.controller;

import java.util.Optional;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.RegistrationVerificationDto;
import com.example.request.LoginRequest;
import com.example.request.PasswordResetMailRequest;
import com.example.request.PasswordResetUpdateRequest;
import com.example.request.PreSignupRequest;
import com.example.request.RegisterUserRequest;
import com.example.service.AuthService;
import com.example.service.AuthService.AuthResult;
import com.example.service.CartService;
import com.example.util.CookieUtil;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
public class AuthController {
    /* TODO:
     * ログアウト実装
     * ログアウトは一旦フロント破棄で行う。
         いずれはリフレッシュトークン＋短命アクセストークンにする
     * ログイン方法が増えたら、SuccessHandler 使う方が良いかも
     * loginメソッド
         将来的にJSONで返す方がよいかも
         Postmanで毎リクエストで自動でJWTが付与されるようにする
         必要な属性後で足す
     * verifyメソッド
         tokenのバリデーションチェックする
         Entity→Responseの詰め替えをコントローラで行ってよいものか？
    */

    private final AuthService authService;

    private final CartService cartService;

    private final CookieUtil cookieUtil;

    // 認証
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse response) {
        AuthResult res = authService.authenticate(req.getUsername(), req.getPassword());

        Optional<String> guestCartId = cookieUtil.extractCartId(httpReq);
        if (guestCartId.isPresent()) {
            String userCartId = cartService.getOrCreateCartId(res.userId());
            cartService.mergeGuestToUser(guestCartId.get(), userCartId);
            cookieUtil.clearCartId(response);
        }
        addJwtCookie(response, res.jwt());

        return ResponseEntity.ok().build();
    }

    // 登録
    @PostMapping("/registration-request")
    public ResponseEntity<Void> send(@RequestBody @Valid PreSignupRequest req) throws MessagingException {
        authService.requestRegistration(req.getEmail());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/registration-verification")
    public ResponseEntity<RegistrationVerificationDto> verify(@RequestParam String rawToken) {
        RegistrationVerificationDto dto = authService.verify(rawToken);
        
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/register/complete")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest req,
            HttpServletResponse res) {
        String jwt = authService.register(req);
        addJwtCookie(res, jwt);

        return ResponseEntity.ok().build();
    }

    // パスワード再設定
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> request(@RequestBody @Valid PasswordResetMailRequest req) {
        authService.sendPasswordRestMail(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/update")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid PasswordResetUpdateRequest req) {
        authService.resetPassword(req);

        return ResponseEntity.ok().build();
    }

    private void addJwtCookie(HttpServletResponse res, String jwt) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
                .path("/")
                //                .maxAge(Duration.ofHours(1))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
