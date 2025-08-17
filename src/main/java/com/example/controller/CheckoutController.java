package com.example.controller;

import jakarta.mail.MessagingException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CheckoutDto;
import com.example.service.CheckoutService;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {
    /*
     * TODO:
     * 購入確定処理も一旦ここに書く。
     */

    private final CheckoutService checkoutService;

    @GetMapping
    public CheckoutDto showCheckout(@AuthenticationPrincipal String userId) {
        return checkoutService.loadCheckout(userId);
    }

    @PostMapping
    public ResponseEntity<Void> checkout(@AuthenticationPrincipal String userId) throws MessagingException {
        checkoutService.checkout(userId);
        return ResponseEntity.ok().build();
    }

}
