package com.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    /*
     * TODO:
     * userIdで毎回expression書いてるが、anonyを無効にしたほうがよいのか？
     * HttpServletRequestをサービスに渡さないほうが良いかも
     * @validatedで発生した例外を400ステータスに変換して返す
     */

    private final CartService cartService;

    @GetMapping
    public CartDto showCart(@RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") String userId,
            HttpServletRequest req) {
        return cartService.showCart(page, userId, req);
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

    @DeleteMapping("/items/{productId}")
    public CartDto removeFromCart(@PathVariable @NotBlank String productId,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") String userId,
            HttpServletRequest req) {
        return cartService.removeItem(productId, userId, req);
    }

    @PutMapping("items/{productId}")
    public CartDto changeQty(@PathVariable String productId,
            @RequestBody @Min(1) @Max(20) int qty,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") String userId,
            HttpServletRequest httpReq) {
        return cartService.changeQty(productId, qty, userId, httpReq);
    }
}
