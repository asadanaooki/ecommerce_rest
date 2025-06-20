package com.example.controller;

import jakarta.validation.constraints.Min;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.FavoritePageDto;
import com.example.service.FavoriteService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
public class FavoriteController {
    
    private final FavoriteService favoriteService;

    @GetMapping("/favorites")
    public FavoritePageDto getFavorites(@RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal String userId) {
        // 認証済み
        return favoriteService.getFavoritePage(userId, page);
    }

}
