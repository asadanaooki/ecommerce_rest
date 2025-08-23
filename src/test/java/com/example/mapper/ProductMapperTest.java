package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.dto.ProductCardDto;
import com.example.dto.ProductDetailDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.enums.SaleStatus;
import com.example.enums.SortType;
import com.example.mapper.ProductMapper.SearchCondition;
import com.example.testUtil.FlywayResetExtension;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductMapperTest {

    @Autowired
    ProductMapper productMapper;
    
    @Test
    void decreaseStock() {
        String productId = "f9c9cfb2-0893-4f1c-b508-f9e909ba5274";
        int rows = productMapper.decreaseStock(productId, 4);
        
        assertThat(rows).isOne();
        
       Product p = productMapper.selectByPrimaryKey(productId);
        assertThat(p.getStock()).isEqualTo(11);
    }

    @Test
    void selectByPrimaryKey() {
        String productId = "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68";
        
        Product product = productMapper.selectByPrimaryKey(productId);
        
        assertThat(product).isNotNull();
        assertThat(product.getProductId()).isEqualTo(productId);
        assertThat(product.getProductName()).isEqualTo("Item19");
        assertThat(product.getPriceExcl()).isEqualTo(750);
        assertThat(product.getStock()).isEqualTo(20);
        assertThat(product.getStatus()).isEqualTo(SaleStatus.PUBLISHED);
    }

    @Test
    void selectViewByPrimaryKey() {
        String productId = "f9c9cfb2-0893-4f1c-b508-f9e909ba5274";
        
        ProductCoreView view = productMapper.selectViewByPrimaryKey(productId);
        
        assertThat(view).isNotNull();
        assertThat(view.getProductId()).isEqualTo(productId);
        assertThat(view.getProductName()).isEqualTo("Item18");
        assertThat(view.getPriceExcl()).isEqualTo(3200);
        assertThat(view.getPriceIncl()).isEqualTo(3520);
        assertThat(view.getStock()).isEqualTo(15);
        assertThat(view.getStatus()).isEqualTo(SaleStatus.PUBLISHED);
    }

    @Test
    void searchProducts_withoutKeywords() {
        List<String> emptyKeywords = Arrays.asList();
        
        SearchCondition sc = new SearchCondition(null, emptyKeywords, SortType.NEW, 5, 0);
        List<ProductCardDto> results = productMapper.searchProducts(sc);
        
        assertThat(results).hasSize(5);
        assertThat(results.get(0).getProductName()).isNotNull();
        assertThat(results.get(0).getPriceIncl()).isPositive();
    }

    @Test
    void searchProducts_withKeywords() {
        List<String> keywords = Arrays.asList("Item1");
        
        SearchCondition sc = new SearchCondition(null, keywords, SortType.LOW, 10, 0);
        List<ProductCardDto> results = productMapper.searchProducts(sc);
        
        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(dto -> {
            assertThat(dto.getProductName()).contains("Item1");
        });
    }

    @Test
    void searchProducts_withUserFavorites() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        List<String> emptyKeywords = Arrays.asList();
        
        // This test assumes some favorites may exist in test data
        
        SearchCondition sc = new SearchCondition(userId, emptyKeywords, SortType.NEW, 10, 0);
        List<ProductCardDto> results = productMapper.searchProducts(sc);
        
        assertThat(results).isNotEmpty();
        
        // Find the favorited product
        ProductCardDto favProduct = results.stream()
            .filter(p -> p.getProductId().equals("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68"))
            .findFirst()
            .orElse(null);
        
        if (favProduct != null) {
            assertThat(favProduct.isFav()).isTrue();
        }
    }

    @Test
    void searchProducts_sortByPriceHigh() {
        List<String> emptyKeywords = Arrays.asList();
        
        SearchCondition sc = new SearchCondition(null, emptyKeywords, SortType.HIGH, 5, 0);
        List<ProductCardDto> results = productMapper.searchProducts(sc);
        
        assertThat(results).hasSize(5);
        
        // Verify descending price order
        for (int i = 1; i < results.size(); i++) {
            assertThat(results.get(i - 1).getPriceIncl())
                .isGreaterThanOrEqualTo(results.get(i).getPriceIncl());
        }
    }

    @Test
    void searchProducts_sortByPriceLow() {
        List<String> emptyKeywords = Arrays.asList();
        
        SearchCondition sc = new SearchCondition(null, emptyKeywords, SortType.LOW, 5, 0);
        List<ProductCardDto> results = productMapper.searchProducts(sc);
        
        assertThat(results).hasSize(5);
        
        // Verify ascending price order
        for (int i = 1; i < results.size(); i++) {
            assertThat(results.get(i - 1).getPriceIncl())
                .isLessThanOrEqualTo(results.get(i).getPriceIncl());
        }
    }

    @Test
    void countProducts_withoutKeywords() {
        List<String> emptyKeywords = Arrays.asList();
        
        int count = productMapper.countProducts(emptyKeywords);
        
        assertThat(count).isPositive();
    }

    @Test
    void countProducts_withKeywords() {
        List<String> keywords = Arrays.asList("Item1");
        
        int count = productMapper.countProducts(keywords);
        
        assertThat(count).isPositive();
        assertThat(count).isLessThanOrEqualTo(11); // Item1, Item10, Item11, etc.
    }

    @Test
    void findProductDetail_withoutUser() {
        String productId = "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68";
        
        ProductDetailDto detail = productMapper.findProductDetail(productId, null);
        
        assertThat(detail).isNotNull();
        assertThat(detail.getProductId()).isEqualTo(productId);
        assertThat(detail.getProductName()).isEqualTo("Item19");
        assertThat(detail.getPriceIncl()).isEqualTo(825);
        assertThat(detail.isFav()).isFalse();
        assertThat(detail.getRatingAvg()).isNotNull();
        assertThat(detail.getReviewCount()).isNotNull();
    }

    @Test
    void findProductDetail_withUser() {
        String productId = "f9c9cfb2-0893-4f1c-b508-f9e909ba5274";
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        
        ProductDetailDto detail = productMapper.findProductDetail(productId, userId);
        
        assertThat(detail).isNotNull();
        assertThat(detail.getProductId()).isEqualTo(productId);
        assertThat(detail.getProductName()).isEqualTo("Item18");
        assertThat(detail.getPriceIncl()).isEqualTo(3520);
        assertThat(detail.getProductDescription()).isNotNull();
        assertThat(detail.isOutOfStock()).isFalse();
    }

    @Test
    void findProductDetail_nonExistentProduct() {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";
        
        ProductDetailDto detail = productMapper.findProductDetail(nonExistentId, null);
        
        assertThat(detail).isNull();
    }

}
