package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.dto.FavoritePageDto;
import com.example.mapper.FavoriteMapper;
import com.example.util.PaginationUtil;
import com.example.util.TaxCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    // TODO:
    // 毎回TaxCalculatorをDIして価格計算するのか？色々なクラスに散らばってる
    // 毎回pazeSize返してるが、コスト的にどうなのか？

    private final FavoriteMapper favoriteMapper;

    @Value("${settings.favorite.size}")
    private int pageSize;

    private final TaxCalculator calculator;

    public FavoritePageDto getFavoritePage(String userId, int page) {
        int offset = PaginationUtil.calculateOffset(page, pageSize);
        int total = favoriteMapper.countFavoritesByUser(userId);

        List<FavoritePageDto.FavoriteRow> rows = favoriteMapper.findFavoritesPage(userId, pageSize, offset)
                .stream()
                .map(p -> new FavoritePageDto.FavoriteRow(
                        p.getProductId(),
                        p.getProductName(),
                        calculator.calculatePriceIncludingTax(p.getPrice()),
                        p.getSaleStatus()))
                .toList();
        
        return new FavoritePageDto(rows, pageSize, total);
    }
    
    public void deleteFavorite(String userId, String productId) {
        favoriteMapper.deleteByPrimaryKey(userId, productId);
    }
}
