package com.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.FavoritePageDto;
import com.example.request.AddCartRequest;
import com.example.service.CartService;
import com.example.service.FavoriteService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    private final CartService cartService;

    @GetMapping
    public FavoritePageDto getFavorites(@RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal String userId) {
        // 認証済み
        return favoriteService.getFavoritePage(userId, page);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addFavorite(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId) {
        // SpringSecurityを通過してるため、必ずuserIdが取得できる
        favoriteService.addFavorite(productId, userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId) {
        favoriteService.deleteFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/cart/{productId}")
    public ResponseEntity<Void> addToCart(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AddCartRequest req) {
        // 必ずログインしてるため、servlet不要
        String cartId = cartService.getOrCreateCartId(userId);
        cartService.addToCart(cartId, userId, productId, req);
        return ResponseEntity.ok().build();
    }

}
