package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.dto.FavoritePageDto;
import com.example.entity.Product;
import com.example.mapper.FavoriteMapper;
import com.example.util.TaxCalculator;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {
    // TODO:
    // calculatorの税率や表示件数を設定ファイルから反映させるようにしたほうがよい？
    
    @Mock
    FavoriteMapper favoriteMapper;
    
    @Spy
    TaxCalculator calculator = new TaxCalculator(10);
    
    @InjectMocks
    FavoriteService favoriteService;
    
    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(favoriteService, "pageSize", 2);
    }

    @Nested
    class GetFavoritePage{
        String userId = UUID.randomUUID().toString();
        
        @Test
        void getFavoritePage_noHit() {
            doReturn(0).when(favoriteMapper).countFavoritesByUser(anyString());
            doReturn(Collections.emptyList()).when(favoriteMapper)
            .findFavoritesPage(anyString(), anyInt(), anyInt());
            
            FavoritePageDto dto = favoriteService.getFavoritePage(userId, 1);
            
            assertThat(dto.getItems()).isEmpty();
            assertThat(dto.getPageSize()).isEqualTo(2);
            assertThat(dto.getTotalItems()).isZero();
        }
        
        @Test
        void getFavoritePage_hits() {
            Product p1 = new Product();
            p1.setProductId("P-001");
            p1.setProductName("商品A");
            p1.setPrice(1000);          // 税込 1,100 になる想定
            p1.setSaleStatus("1");

            Product p2 = new Product();
            p2.setProductId("P-002");
            
            doReturn(3).when(favoriteMapper).countFavoritesByUser(userId);
            doReturn(List.of(p1, p2)).when(favoriteMapper)
            .findFavoritesPage(anyString(), anyInt(), anyInt());
            
            FavoritePageDto dto = favoriteService.getFavoritePage(userId,1);
            
            assertThat(dto.getItems()).hasSize(2);
            FavoritePageDto.FavoriteRow row1 = dto.getItems().get(0);
            assertThat(row1.getProductId()).isEqualTo("P-001");
            assertThat(row1.getProductName()).isEqualTo("商品A");
            assertThat(row1.getPrice()).isEqualTo(1100);
            assertThat(row1.getSaleStatus()).isEqualTo("1");
            
            assertThat(dto.getItems().get(1).getProductId()).isEqualTo("P-002");
            
            assertThat(dto.getPageSize()).isEqualTo(2);
            assertThat(dto.getTotalItems()).isEqualTo(3);
        }
    }


}
