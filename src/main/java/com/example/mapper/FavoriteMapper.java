package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.FavoritePageDto;
import com.example.entity.Favorite;

@Mapper
public interface FavoriteMapper {

    int insert(Favorite fav);
    
    int deleteByPrimaryKey(String userId, String productId);
    
    int deleteByUserId(String userId);
    
    List<FavoritePageDto.FavoriteRow> findFavoritesPage(String userId, int limit, int offset);
    
    int countFavoritesByUser(String userId);
}
