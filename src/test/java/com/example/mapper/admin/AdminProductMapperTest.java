package com.example.mapper.admin;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.jdbc.Sql;

import com.example.dto.admin.AdminProductDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.enums.ProductSortField;
import com.example.enums.SaleStatus;
import com.example.enums.SortDirection;
import com.example.request.admin.ProductSearchRequest;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
@Sql(scripts = {
        "/cleanup.sql"
})
class AdminProductMapperTest {

    @Autowired
    AdminProductMapper adminProductMapper;

    @Autowired
    TestDataFactory factory;


    @Nested
    class CountProducts {
        @ParameterizedTest
        @MethodSource("provideSingleFilterAndBoundaryCases")
        void countProducts_singleFilterAndBoundary(Consumer<TestDataFactory> insertMismatch,
                Consumer<ProductSearchRequest> customizeReq, int expected) {
            insertMismatch.accept(factory);

            ProductSearchRequest req = new ProductSearchRequest() {
                {
                    setSortFIeld(ProductSortField.UPDATED_AT);
                    setSortDirection(SortDirection.DESC);
                }
            };
            customizeReq.accept(req);

            assertThat(adminProductMapper.countProducts(req)).isEqualTo(expected);
        }

        static Stream<Arguments> provideSingleFilterAndBoundaryCases() {
            return Stream.of(
                    // フィルタなし
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> {
                                f.createProduct(buildProduct(p -> {})); // base product
                                f.createProduct(buildProduct(p -> {
                                    p.setProductName("テスト");
                                    p.setStatus(SaleStatus.PUBLISHED);
                                }));
                            },
                            (Consumer<ProductSearchRequest>) r -> {
                            },
                            2),
                    // keyword - search for "tem" should match "BaseItem"
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> {})), // creates "BaseItem" which contains "tem"
                            (Consumer<ProductSearchRequest>) r -> r.setQ("tem"),
                            1),
                    // minPrice - product with price 1000 should match minPrice 300
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // price 1000 >= 300
                            (Consumer<ProductSearchRequest>) r -> r.setMinPrice(300),
                            1),
                    // maxPrice - product with price 1000 should match maxPrice 2000
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // price 1000 <= 2000
                            (Consumer<ProductSearchRequest>) r -> r.setMaxPrice(2000),
                            1),
                    // minStock - product with stock 100 should match minAvailable 70
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // stock 100 >= 70
                            (Consumer<ProductSearchRequest>) r -> r.setMinAvailable(70),
                            1),
                    // maxStock - product with stock 100 should match maxAvailable 150
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // stock 100 <= 150
                            (Consumer<ProductSearchRequest>) r -> r.setMaxAvailable(150),
                            1),
                    // createdFrom - product created 2020-01-01 should match createdFrom 2019-03-03
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> {})), // created 2020-01-01 >= 2019-03-03
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2019, 3, 3)),
                            1),
                    // createdTo - product created 2020-01-01 should match createdTo 2022-04-01
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> {})), // created 2020-01-01 <= 2022-04-01
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2022, 4, 1)),
                            1),
                    // updatedFrom - product updated 2021-06-03 should match updatedFrom 2019-12-26
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> {})), // updated 2021-06-03 >= 2019-12-26
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2019, 12, 26)),
                            1),
                    // updatedTo - product updated 2021-06-03 should match updatedTo 2021-07-01
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> {})), // updated 2021-06-03 <= 2021-07-01
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedTo(LocalDate.of(2021, 7, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> {})), // default is PUBLISHED
                            (Consumer<ProductSearchRequest>) r -> r.setStatus(SaleStatus.PUBLISHED),
                            1),

                    // 境界値
                    // minPrice - product with price 1000 should NOT match minPrice 1001 (boundary test)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // price 1000 < 1001
                            (Consumer<ProductSearchRequest>) r -> r.setMinPrice(1001),
                            0),
                    // maxPrice - product with price 1000 should NOT match maxPrice 999 (boundary test)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // price 1000 > 999
                            (Consumer<ProductSearchRequest>) r -> r.setMaxPrice(999),
                            0),
                    // minStock - product with stock 100 should NOT match minAvailable 101 (boundary test)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // stock 100 < 101
                            (Consumer<ProductSearchRequest>) r -> r.setMinAvailable(101),
                            0),
                    // maxStock - product with stock 100 should NOT match maxAvailable 99 (boundary test)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {})), // stock 100 > 99
                            (Consumer<ProductSearchRequest>) r -> r.setMaxAvailable(99),
                            0),
                    // createdFrom - product created 2020-01-01 should match createdFrom 2020-01-01 (exact boundary)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(p -> {})), // created 2020-01-01 == 2020-01-01
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            1),
                    // createdTo - product created 2020-01-01 should match createdTo 2020-01-01 (exact boundary)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(p -> {})), // created 2020-01-01 == 2020-01-01
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            1),
                    // updatedFrom - product updated 2021-06-03 should match updatedFrom 2021-06-03 (exact boundary)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> {})), // updated 2021-06-03 == 2021-06-03
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2021, 6, 3)),
                            1),
                    // updatedTo - product updated 2021-06-03 should match updatedTo 2021-06-03 (exact boundary)
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> {})), // updated 2021-06-03 == 2021-06-03
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedTo(LocalDate.of(2021, 6, 3)),
                            1));
        }

        @ParameterizedTest
        @MethodSource("provideKeywordCases")
        void countProducts_keywordFilter(
                Consumer<TestDataFactory> insertMatching,
                Consumer<ProductSearchRequest> customizeReq,
                int expected) {
            insertMatching.accept(factory);

            ProductSearchRequest req = new ProductSearchRequest() {
                {
                    setSortFIeld(ProductSortField.UPDATED_AT);
                    setSortDirection(SortDirection.DESC);
                }
            };
            customizeReq.accept(req);

            assertThat(adminProductMapper.countProducts(req)).isEqualTo(expected);
        }

        static Stream<Arguments> provideKeywordCases() {
            return Stream.of(
                    // sku
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {
                            })),
                            (Consumer<ProductSearchRequest>) r -> r.setQ("0001"),
                            1),
                    // productName
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("apple"))),
                            (Consumer<ProductSearchRequest>) r -> r.setQ("le"),
                            1),
                    // 複数ワード
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> {
                                f.createProduct(buildProduct(p -> {})); // base product with sku containing "1"
                                f.createProduct(buildProduct(p -> p.setProductName("NMH"))); // product with name containing "N"
                            },
                            (Consumer<ProductSearchRequest>) r -> r.setQ("1 N"),
                            2));
        }
    }

    @Nested
    class SearchProducts {

        private int limit = 2;

        @Test
        void searchProducts_withFilter() {
            factory.createProduct(buildProduct(p -> {})); // base product "BaseItem"
            factory.createProduct(buildProduct(p -> {
                p.setProductName("aaa");
                p.setStatus(SaleStatus.UNPUBLISHED);
            }));
            factory.createProduct(buildProduct(p -> p.setProductName("えおい")));

            ProductSearchRequest req = new ProductSearchRequest();
            req.setStatus(SaleStatus.PUBLISHED);
            req.setSortFIeld(ProductSortField.NAME);
            req.setSortDirection(SortDirection.DESC);

            List<AdminProductDto> results = adminProductMapper.searchProducts(req, limit, 0);

            assertThat(results).hasSize(limit)
                    .extracting(AdminProductDto::getProductName)
                    .containsExactly("えおい", "BaseItem");
        }

        @Test
        void searchProducts_noFilter() {
            factory.createProduct(buildProduct(p -> {})); // base product
            factory.createProduct(buildProduct(p -> {
                p.setProductId("5083a5da-4ab0-4000-a390-68c94fc58052");
                p.setProductName("test");
                p.setPriceExcl(200);
                p.setStatus(SaleStatus.UNPUBLISHED);
            }));
            factory.createProduct(buildProduct(p -> {
                p.setProductId("c32d16ad-2e69-47bf-bc85-933169754fcd");
                p.setProductName("test2");
                p.setPriceExcl(200);
                p.setStatus(SaleStatus.PUBLISHED);
            }));

            ProductSearchRequest req = new ProductSearchRequest();
            req.setSortFIeld(ProductSortField.PRICE);
            req.setSortDirection(SortDirection.ASC);

            List<AdminProductDto> results = adminProductMapper.searchProducts(req, 3, 0);

            assertThat(results).hasSize(3)
                    .extracting(AdminProductDto::getProductId)
                    .contains("5083a5da-4ab0-4000-a390-68c94fc58052", "c32d16ad-2e69-47bf-bc85-933169754fcd");
        }
    }

    static Product buildProduct(Consumer<Product> customizer) {
        Product p = new Product();
        p.setProductId(UUID.randomUUID().toString());
        p.setProductName("BaseItem");
        p.setProductDescription("desc");
        p.setPriceExcl(1000);
        p.setStock(100);
        p.setReserved(0);
        p.setStatus(SaleStatus.PUBLISHED);
        p.setVersion(1);
        p.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
        p.setUpdatedAt(LocalDateTime.of(2021, 6, 3, 0, 0, 0));
        customizer.accept(p);
        return p;
    }

}
