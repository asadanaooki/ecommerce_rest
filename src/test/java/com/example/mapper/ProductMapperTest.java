package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.example.dto.ProductCardDto;
import com.example.dto.ProductDetailDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.enums.ProductSortOption;
import com.example.enums.SaleStatus;
import com.example.mapper.ProductMapper.SearchCondition;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
class ProductMapperTest {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    TestDataFactory factory;

    @Nested
    class DecreaseStock {
        String productId = UUID.randomUUID().toString();

        @ParameterizedTest(name = "{0}")
        @MethodSource("successCases")
        void decreaseStock_success(String label,
                int stock,
                Integer reserved,
                int qty,
                boolean withVersion) {
            Product p = new Product();
            p.setProductId(productId);
            p.setProductName("TestItem");
            p.setProductDescription("for success case");
            p.setPriceExcl(1000);
            p.setStock(stock);
            p.setReserved(reserved);
            p.setStatus(SaleStatus.PUBLISHED);
            p.setVersion(2);
            factory.createProduct(p);

            Integer versionArg = withVersion ? p.getVersion() : null;

            int updated = productMapper.decreaseStock(productId, qty, versionArg);
            assertThat(updated).isOne();

            Product after = productMapper.selectByPrimaryKey(productId);
            assertThat(after.getStock()).isEqualTo(stock - qty);
            assertThat(after.getReserved()).isEqualTo(reserved);
        }

        static Stream<Arguments> successCases() {
            return Stream.of(
                    Arguments.of("versionあり・在庫十分", 15, 3, 4, true),
                    Arguments.of("versionなし・reserved=NULL", 15, null, 4, false),
                    Arguments.of("境界OK：stock-qty==reserved", 15, 10, 5, false));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("failureCases")
        void decreaseStock_failure(String label,
                int stock,
                Integer reserved,
                int qty,
                Function<Product, Integer> versionProvider,
                boolean useNonExistentId) {
            Product p = new Product();
            p.setProductId(productId);
            p.setProductName("TestItem");
            p.setProductDescription("for failure case");
            p.setPriceExcl(1000);
            p.setStock(stock);
            p.setReserved(reserved);
            p.setStatus(SaleStatus.PUBLISHED);
            p.setVersion(2);
            factory.createProduct(p);

            String targetId = useNonExistentId ? "00000000-0000-0000-0000-000000000000"
                    : productId;

            Integer versionArg = versionProvider.apply(p);

            int updated = productMapper.decreaseStock(targetId, qty, versionArg);
            assertThat(updated).isZero();

            Product after = productMapper.selectByPrimaryKey(productId);
            assertThat(after.getStock()).isEqualTo(stock);
            assertThat(after.getReserved()).isEqualTo(reserved);
        }

        static Stream<Arguments> failureCases() {
            return Stream.of(
                    Arguments.of("productId不在", 15, 3, 4,
                            (Function<Product, Integer>) p -> p.getVersion(), true),
                    Arguments.of("在庫不足（versionあり）", 15, 3, 14,
                            (Function<Product, Integer>) p -> p.getVersion(), false),
                    Arguments.of("version不一致", 15, 3, 4,
                            (Function<Product, Integer>) p -> p.getVersion() + 1, false),
                    Arguments.of("境界NG：stock-qty==reserved-1", 15, 6, 10,
                            (Function<Product, Integer>) p -> p.getVersion(), false));
        }
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

        SearchCondition sc = new SearchCondition(null, emptyKeywords, ProductSortOption.NEW, 5, 0);
        List<ProductCardDto> results = productMapper.searchProducts(sc);

        assertThat(results).hasSize(5);
        assertThat(results.get(0).getProductName()).isNotNull();
        assertThat(results.get(0).getPriceIncl()).isPositive();
    }

    @Test
    void searchProducts_withKeywords() {
        List<String> keywords = Arrays.asList("Item1");

        SearchCondition sc = new SearchCondition(null, keywords, ProductSortOption.LOW, 10, 0);
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

        SearchCondition sc = new SearchCondition(userId, emptyKeywords, ProductSortOption.NEW, 10, 0);
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

        SearchCondition sc = new SearchCondition(null, emptyKeywords, ProductSortOption.HIGH, 5, 0);
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

        SearchCondition sc = new SearchCondition(null, emptyKeywords, ProductSortOption.LOW, 5, 0);
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
