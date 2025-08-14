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

    @BeforeEach
    void setup() {
        factory.createProduct(buildProduct(p -> {
        }));
    }

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
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> {
                                p.setProductName("テスト");
                                p.setStatus(SaleStatus.PUBLISHED);
                            })),
                            (Consumer<ProductSearchRequest>) r -> {
                            },
                            2),
                    // keyword
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("apple"))),
                            (Consumer<ProductSearchRequest>) r -> r.setQ("tem"),
                            1),
                    // minPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPrice(50))),
                            (Consumer<ProductSearchRequest>) r -> r.setMinPrice(300),
                            1),
                    // maxPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPrice(3000))),
                            (Consumer<ProductSearchRequest>) r -> r.setMaxPrice(2000),
                            1),
                    // minStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(50))),
                            (Consumer<ProductSearchRequest>) r -> r.setMinAvailable(70),
                            1),
                    // maxStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(200))),
                            (Consumer<ProductSearchRequest>) r -> r.setMaxAvailable(150),
                            1),
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2018, 8, 2, 10, 44, 3)))),
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2019, 3, 3)),
                            1),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2025, 7, 2, 10, 44, 3)))),
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2022, 4, 1)),
                            1),
                    // updatedFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2019, 12, 24, 10, 44, 3)))),
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2019, 12, 26)),
                            1),
                    // updatedTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 7, 2, 10, 44, 3)))),
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedTo(LocalDate.of(2021, 7, 1)),
                            1),
                    // status
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setStatus(SaleStatus.UNPUBLISHED))),
                            (Consumer<ProductSearchRequest>) r -> r.setStatus(SaleStatus.PUBLISHED),
                            1),

                    // 境界値
                    // minPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPrice(999))),
                            (Consumer<ProductSearchRequest>) r -> r.setMinPrice(1000),
                            1),
                    // maxPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPrice(1001))),
                            (Consumer<ProductSearchRequest>) r -> r.setMaxPrice(1000),
                            1),
                    // minStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(99))),
                            (Consumer<ProductSearchRequest>) r -> r.setMinAvailable(100),
                            1),
                    // maxStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(101))),
                            (Consumer<ProductSearchRequest>) r -> r.setMaxAvailable(100),
                            1),
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2019, 12, 31, 1, 2, 3)))),
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            1),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2020, 1, 2, 1, 4, 3)))),
                            (Consumer<ProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            1),
                    // updatedFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 6, 2, 3, 10, 2)))),
                            (Consumer<ProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2021, 6, 3)),
                            1),
                    // updatedTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 6, 4, 10, 2, 3)))),
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
                            (Consumer<ProductSearchRequest>) r -> r.setQ("2"),
                            1),
                    // productName
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("apple"))),
                            (Consumer<ProductSearchRequest>) r -> r.setQ("le"),
                            1),
                    // 複数ワード
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("NMH"))),
                            (Consumer<ProductSearchRequest>) r -> r.setQ("1 N"),
                            2));
        }
    }

    @Nested
    class SearchProducts {

        private int limit = 2;

        @Test
        void searchProducts_withFilter() {
            factory.createProduct(buildProduct(p -> {
                p.setProductName("aaa");
                p.setStatus(SaleStatus.UNPUBLISHED);
            }));
            factory.createProduct(buildProduct(p -> p.setProductName("えおい")));

            ProductSearchRequest req = new ProductSearchRequest();
            req.setStatus(SaleStatus.PUBLISHED);
            req.setSortFIeld(ProductSortField.NAME);
            req.setSortDirection(SortDirection.DESC);

            List<ProductCoreView> results = adminProductMapper.searchProducts(req, limit, 0);

            assertThat(results).hasSize(limit)
                    .extracting(ProductCoreView::getProductName)
                    .containsExactly("えおい", "BaseItem");
        }

        @Test
        void searchProducts_noFilter() {
            factory.createProduct(buildProduct(p -> {
                p.setProductId("5083a5da-4ab0-4000-a390-68c94fc58052");
                p.setProductName("test");
                p.setPrice(200);
                p.setStatus(SaleStatus.UNPUBLISHED);
            }));
            factory.createProduct(buildProduct(p -> {
                p.setProductId("c32d16ad-2e69-47bf-bc85-933169754fcd");
                p.setProductName("test2");
                p.setPrice(200);
                p.setStatus(SaleStatus.PUBLISHED);
            }));

            ProductSearchRequest req = new ProductSearchRequest();
            req.setSortFIeld(ProductSortField.PRICE);
            req.setSortDirection(SortDirection.ASC);

            List<ProductCoreView> results = adminProductMapper.searchProducts(req, limit, 0);

            assertThat(results).hasSize(limit)
                    .extracting(ProductCoreView::getProductId)
                    .containsExactly("5083a5da-4ab0-4000-a390-68c94fc58052", "c32d16ad-2e69-47bf-bc85-933169754fcd");
        }
    }

    static Product buildProduct(Consumer<Product> customizer) {
        Product p = new Product();
        p.setProductId(UUID.randomUUID().toString());
        p.setProductName("BaseItem");
        p.setProductDescription("desc");
        p.setPrice(1000);
        p.setStock(100);
        p.setStatus(SaleStatus.PUBLISHED);
        p.setCreatedAt(LocalDateTime.of(2020, 1, 1, 10, 3, 4));
        p.setUpdatedAt(LocalDateTime.of(2021, 6, 3, 10, 40, 5));
        customizer.accept(p);
        return p;
    }

}
