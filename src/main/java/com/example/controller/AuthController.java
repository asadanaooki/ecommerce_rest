package com.example.controller;

import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.PreRegistration;
import com.example.request.LoginRequest;
import com.example.request.PreSignupRequest;
import com.example.request.RegisterUserRequest;
import com.example.response.ApiResponse;
import com.example.response.VerifyTokenResponse;
import com.example.service.AuthService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public String login(@RequestBody @Valid LoginRequest req) {
        // TODO:
        // 将来的にJSONで返す方がよいかも
        return authService.authenticate(req.getUsername(), req.getPassword());
    }

    @PostMapping("/register/email")
    public ApiResponse send(@RequestBody @Valid PreSignupRequest req) throws MessagingException {
        authService.sendRegistrationUrl(req.getEmail());

        return new ApiResponse("signup.email.sent");
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
                    .map(f -> new FieldErrorInfo(f.getField(), f.getDefaultMessage())).toList();
            return ResponseEntity.badRequest().body(errors);
        }
        authService.register(req);
        return ResponseEntity.ok().build();
    }

    public static record FieldErrorInfo(String field, String messageKey) {
    }

}
