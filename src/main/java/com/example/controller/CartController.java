package com.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.request.AddCartRequest;
import com.example.service.CartService;
import com.example.util.CookieUtil;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    
    private final CookieUtil cookieUtil;
    
    @PostMapping
    public ResponseEntity<Void> addToCart(@Valid @RequestBody AddCartRequest req,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") String userId,
            HttpServletRequest httpReq, HttpServletResponse httpRes) {
        String cartId;
        if (userId == null) {
            cartId = cookieUtil.ensureCartId(httpReq, httpRes);
        } else {
            cartId = cartService.findOrCreateUserCart(userId);
        }
        // TODO:
        // reqをMapperまで渡しても現状問題ないが、将来的に詰め替えた方がいい？
        cartService.addToCart(cartId, userId, req);

        return ResponseEntity.ok().build();
    }
}
