package com.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CartDto;
import com.example.request.AddCartRequest;
import com.example.service.CartService;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    
    @GetMapping
    public CartDto showCart(@RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") String userId,
            HttpServletRequest req,
            HttpServletResponse res
            ) {
        return cartService.showCart(req, res, userId, page);
    }
    
    @PostMapping("/items")
    public ResponseEntity<Void> addToCart(@Valid @RequestBody AddCartRequest req,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") String userId,
            HttpServletRequest httpReq, HttpServletResponse httpRes) {
        // TODO:
        // reqをMapperまで渡しても現状問題ないが、将来的に詰め替えた方がいい？
        // サーブレットをサービス層に渡してるが、将来的に別の実装にした方が良いかも
        cartService.addToCart(httpReq, httpRes, userId, req);

        return ResponseEntity.ok().build();
    }
}
