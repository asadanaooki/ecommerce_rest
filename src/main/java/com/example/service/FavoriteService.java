package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.converter.FavoriteConverter;
import com.example.dto.FavoritePageDto;
import com.example.mapper.FavoriteMapper;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    // TODO:
    // 毎回pazeSize返してるが、コスト的にどうなのか？
    // 削除時、位置かえないようにする

    private final FavoriteMapper favoriteMapper;
    
    private final FavoriteConverter favoriteConverter;

    @Value("${settings.favorite.size}")
    private int pageSize;

    public FavoritePageDto getFavoritePage(String userId, int page) {
        int offset = PaginationUtil.calculateOffset(page, pageSize);
        int total = favoriteMapper.countFavoritesByUser(userId);

        List<FavoritePageDto.FavoriteRow> rows = favoriteConverter.toRowList(
                favoriteMapper.findFavoritesPage(userId, pageSize, offset));
        
        return new FavoritePageDto(rows, pageSize, total);
    }
    
    public void deleteFavorite(String userId, String productId) {
        favoriteMapper.deleteByPrimaryKey(userId, productId);
    }
}
