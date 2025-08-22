package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.dto.FavoritePageDto;
import com.example.entity.Favorite;
import com.example.entity.Product;
import com.example.enums.SaleStatus;
import com.example.testUtil.FlywayResetExtension;
import com.example.util.PaginationUtil;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FavoriteMapperTest {
    @Autowired
    FavoriteMapper favoriteMapper;

    final int PAGE_SIZE = 2;

    @Nested
    class FindFavoritesPage {
        String userId = "550e8400-e29b-41d4-a716-446655440000";

        @BeforeEach
        void setup() {
            favoriteMapper.deleteByUserId(userId);
            favoriteMapper.insert(new Favorite() {
                {
                    setUserId(userId);
                    setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                    setCreatedAt(LocalDateTime.of(2025, 3, 7, 0, 0));
                }
            });
            favoriteMapper.insert(new Favorite() {
                {
                    setUserId(userId);
                    setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
                    setCreatedAt(LocalDateTime.of(2025, 1, 7, 10, 0));
                }
            });
            favoriteMapper.insert(new Favorite() {
                {
                    setUserId(userId);
                    setProductId("4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07");
                    setCreatedAt(LocalDateTime.of(2025, 1, 7, 8, 20));
                }
            });
            favoriteMapper.insert(new Favorite() {
                {
                    setUserId(userId);
                    setProductId("6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd");
                    setCreatedAt(LocalDateTime.of(1994, 5, 14, 10, 0));
                }
            });
            favoriteMapper.insert(new Favorite() {
                {
                    setUserId(userId);
                    setProductId("09d5a43a-d24c-41c7-af2b-9fb7b0c9e049");
                    setCreatedAt(LocalDateTime.of(1991, 8, 14, 10, 0));
                }
            });
        }

        @Test
        void findFavoritesPage_firstPage() {
            List<FavoritePageDto.FavoriteRow> list = favoriteMapper.findFavoritesPage(userId, PAGE_SIZE,
                    PaginationUtil.calculateOffset(1, PAGE_SIZE));

            assertThat(list).hasSize(PAGE_SIZE)
                    .first()
                    .extracting(FavoritePageDto.FavoriteRow::getProductId,
                            FavoritePageDto.FavoriteRow::getProductName,
                            FavoritePageDto.FavoriteRow::getPriceIncl,
                            FavoritePageDto.FavoriteRow::getStatus)
                    .containsExactly("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68",
                            "Item19",
                            750,
                            SaleStatus.PUBLISHED);
            assertThat(list.get(1).getProductId()).isEqualTo("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");

        }

        @Test
        void findFavoritesPage_lastPage() {
            List<FavoritePageDto.FavoriteRow> list = favoriteMapper.findFavoritesPage(userId, PAGE_SIZE,
                    PaginationUtil.calculateOffset(3, PAGE_SIZE));

            assertThat(list)
                    .extracting(FavoritePageDto.FavoriteRow::getProductId)
                    .containsExactly("09d5a43a-d24c-41c7-af2b-9fb7b0c9e049");
        }

        @Test
        void findFavoritesPage_overPage() {
            List<FavoritePageDto.FavoriteRow> list = favoriteMapper.findFavoritesPage(userId, PAGE_SIZE,
                    PaginationUtil.calculateOffset(4, PAGE_SIZE));
            assertThat(list).hasSize(0);
        }
    }

}
