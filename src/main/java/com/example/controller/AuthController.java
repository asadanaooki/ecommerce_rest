package com.example.controller;

import java.util.List;
import java.util.Optional;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.PreRegistration;
import com.example.request.LoginRequest;
import com.example.request.PasswordResetMailRequest;
import com.example.request.PasswordResetUpdateRequest;
import com.example.request.PreSignupRequest;
import com.example.request.RegisterUserRequest;
import com.example.response.VerifyTokenResponse;
import com.example.service.AuthService;
import com.example.service.AuthService.AuthResult;
import com.example.service.CartService;
import com.example.util.CookieUtil;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AuthController {
    /* TODO:
       ログアウトは一旦フロント破棄で行う。
       いずれはリフレッシュトークン＋短命アクセストークンにする
       ログイン方法が増えたら、SuccessHandler 使う方が良いかも
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
            String userCartId = cartService.findOrCreateUserCart(res.userId());
            cartService.mergeGuestToUser(guestCartId.get(), userCartId);
            cookieUtil.clearCartCookie(response);
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
        // Entity→Responseの詰め替えをコントローラで行ってよいものか？
        PreRegistration pr = authService.verify(token);
        return new VerifyTokenResponse(token, pr.getEmail());
    }

    @PostMapping("/register/complete")
    public ResponseEntity<List<FieldErrorInfo>> register(@RequestBody @Valid RegisterUserRequest req,
            BindingResult result) {
        // TODO:
        // 将来的に各フィールドfail-fastのバリデーションチェックにする
        if (result.hasErrors()) {
            List<FieldErrorInfo> errors = result.getFieldErrors().stream()
                    .map(f -> new FieldErrorInfo(f.getField(),
                            f.getCode()))
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }
        authService.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> request(@RequestBody @Valid PasswordResetMailRequest req) {
        authService.sendPasswordRestMail(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/update")
    public ResponseEntity<List<FieldErrorInfo>> request(@RequestBody @Valid PasswordResetUpdateRequest req,
            BindingResult result) {
        // TODO:
        // 将来的に各フィールドfail-fastのバリデーションチェックにする
        if (result.hasErrors()) {
            List<FieldErrorInfo> errors = result.getFieldErrors().stream()
                    .map(f -> new FieldErrorInfo(f.getField(),
                            f.getCode()))
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }
        authService.resetPassword(req);

        return ResponseEntity.ok().build();
    }
    

    public record FieldErrorInfo(String field, String errorCode) {}

}
