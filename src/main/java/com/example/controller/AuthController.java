package com.example.controller;

import java.util.List;
import java.util.Optional;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.PreRegistration;
import com.example.error.FieldErrorInfo;
import com.example.request.EmailChangeRequest;
import com.example.request.LoginRequest;
import com.example.request.PasswordChangeRequest;
import com.example.request.PasswordResetMailRequest;
import com.example.request.PasswordResetUpdateRequest;
import com.example.request.PreSignupRequest;
import com.example.request.ProfileUpdateRequest;
import com.example.request.RegisterUserRequest;
import com.example.response.VerifyTokenResponse;
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
       ログアウトは一旦フロント破棄で行う。
       いずれはリフレッシュトークン＋短命アクセストークンにする
       ログイン方法が増えたら、SuccessHandler 使う方が良いかも
       Rest視点でパス名どうするか？現状、動詞を含めてる
       プロフィール表示(GET)のAPIがない
       GODクラスだから、分割する
    */
    private final AuthService authService;

    private final CartService cartService;

    private final CookieUtil cookieUtil;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse response) {
        // TODO:
        // 将来的にJSONで返す方がよいかも
        // Postmanで毎リクエストで自動でJWTが付与されるようにする
        AuthResult res = authService.authenticate(req.getUsername(), req.getPassword());

        Optional<String> guestCartId = cookieUtil.extractCartId(httpReq);
        if (guestCartId.isPresent()) {
            String userCartId = cartService.getOrCreateCartId(res.userId());
            cartService.mergeGuestToUser(guestCartId.get(), userCartId);
            cookieUtil.clearCartId(response);
        }

        // TODO:
        // 必要な属性後で足す
        ResponseCookie cookie = ResponseCookie.from("accessToken", res.jwt())
                .path("/")
                //                .maxAge(Duration.ofHours(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/email")
    public ResponseEntity<Void> send(@RequestBody @Valid PreSignupRequest req) throws MessagingException {
        authService.sendRegistrationUrl(req.getEmail());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/register/verify")
    public VerifyTokenResponse verify(@RequestParam String token) {
        // TODO:
        // tokenのバリデーションチェックする
        // Entity→Responseの詰め替えをコントローラで行ってよいものか？
        PreRegistration pr = authService.verify(token);
        return new VerifyTokenResponse(token, pr.getEmail());
    }

    @PostMapping("/register/complete")
    public ResponseEntity<List<FieldErrorInfo>> register(@RequestBody @Valid RegisterUserRequest req) {
        authService.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> request(@RequestBody @Valid PasswordResetMailRequest req) {
        authService.sendPasswordRestMail(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/update")
    public ResponseEntity<List<FieldErrorInfo>> resetPassword(
            @RequestBody @Valid PasswordResetUpdateRequest req) {
        authService.resetPassword(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/email-change/request")
    public ResponseEntity<List<FieldErrorInfo>> reqeustEmailChange(@AuthenticationPrincipal String userId,
            @RequestBody @Valid EmailChangeRequest req) {
        authService.requestEmailChange(userId, req);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/email-change/complete")
    public ResponseEntity<Void> completeEmailChange(
            @RequestParam @NotBlank @Size(min = 22, max = 22) String token) {
        authService.completeEmailChange(token);

        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/profile/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal String userId,
            @RequestBody @Valid PasswordChangeRequest req) {
        authService.changePassword(userId,req);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal String userId,
            @RequestBody @Valid ProfileUpdateRequest req) {
        authService.updateProfile(userId, req);
        
        return ResponseEntity.ok().build();
    }

}
