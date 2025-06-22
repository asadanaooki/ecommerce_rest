package com.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.FavoritePageDto;
import com.example.request.AddCartRequest;
import com.example.service.CartService;
import com.example.service.FavoriteService;

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
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable String productId,
            @AuthenticationPrincipal String userId){
        favoriteService.deleteFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/cart")
    public ResponseEntity<Void> addToCart(@Valid @RequestBody AddCartRequest req,
            @AuthenticationPrincipal String userId) {
        String cartId = cartService.findOrCreateUserCart(userId);
        cartService.addToCart(cartId, userId, req);
        return ResponseEntity.ok().build();
    }
    

}
