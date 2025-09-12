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
import com.example.enums.ProductSortField;
import com.example.enums.SaleStatus;
import com.example.enums.SortDirection;
import com.example.request.admin.AdminProductSearchRequest;
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
                Consumer<AdminProductSearchRequest> customizeReq, int expected) {
            insertMismatch.accept(factory);

            AdminProductSearchRequest req = new AdminProductSearchRequest() {
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
                                p.setProductName("DifferentProduct");
                                p.setStatus(SaleStatus.PUBLISHED);
                            })),
                            (Consumer<AdminProductSearchRequest>) r -> {
                            },
                            2),
                    // keyword
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("apple"))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setQ("tem"),
                            1),
                    // minPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPriceExcl(200))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMinPrice(300),
                            1),
                    // maxPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPriceExcl(3000))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMaxPrice(2000),
                            1),
                    // minStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(50))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMinAvailable(70),
                            1),
                    // maxStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(200))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMaxAvailable(150),
                            1),
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2018, 1, 1, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2019, 3, 3)),
                            1),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2022, 4, 1)),
                            1),
                    // updatedFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2018, 1, 1, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2019, 12, 26)),
                            1),
                    // updatedTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2022, 1, 1, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setUpdatedTo(LocalDate.of(2021, 7, 1)),
                            1),
                    // status
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setStatus(SaleStatus.UNPUBLISHED))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setStatus(SaleStatus.PUBLISHED),
                            1),

                    // 境界値
                    // minPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPriceExcl(999))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMinPrice(1000),
                            1),
                    // maxPrice
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setPriceExcl(1001))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMaxPrice(1000),
                            1),
                    // minStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(99))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMinAvailable(100),
                            1),
                    // maxStock
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(buildProduct(p -> p.setStock(101))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setMaxAvailable(100),
                            1),
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(
                                                    p -> p.setCreatedAt(LocalDateTime.of(2019, 12, 31, 23, 59, 59)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            2),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(p -> p.setCreatedAt(LocalDateTime.of(2020, 1, 2, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(
                                            buildProduct(
                                                    p -> p.setCreatedAt(LocalDateTime.of(2020, 1, 1, 23, 59, 59)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            2),
                    // updatedFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 6, 2, 23, 59, 59)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2021, 6, 3)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 6, 3, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setUpdatedFrom(LocalDate.of(2021, 6, 3)),
                            2),
                    // updatedTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 6, 4, 0, 0)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setUpdatedTo(LocalDate.of(2021, 6, 3)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createProduct(
                                    buildProduct(p -> p.setUpdatedAt(LocalDateTime.of(2021, 6, 3, 23, 59, 59)))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setUpdatedTo(LocalDate.of(2021, 6, 3)),
                            2));
        }

        @ParameterizedTest
        @MethodSource("provideKeywordCases")
        void countProducts_keywordFilter(
                Consumer<TestDataFactory> insertMatching,
                Consumer<AdminProductSearchRequest> customizeReq,
                int expected) {
            insertMatching.accept(factory);

            AdminProductSearchRequest req = new AdminProductSearchRequest() {
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
                            (Consumer<AdminProductSearchRequest>) r -> r.setQ("2"),
                            1),
                    // productName
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("apple"))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setQ("le"),
                            1),
                    // 複数ワード
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createProduct(buildProduct(p -> p.setProductName("NMH"))),
                            (Consumer<AdminProductSearchRequest>) r -> r.setQ("1 N"),
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

            AdminProductSearchRequest req = new AdminProductSearchRequest();
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

            AdminProductSearchRequest req = new AdminProductSearchRequest();
            req.setSortFIeld(ProductSortField.PRICE);
            req.setSortDirection(SortDirection.ASC);

            List<AdminProductDto> results = adminProductMapper.searchProducts(req, limit, 0);

            assertThat(results).hasSize(limit)
                    .extracting(AdminProductDto::getProductId)
                    .containsExactly("5083a5da-4ab0-4000-a390-68c94fc58052", "c32d16ad-2e69-47bf-bc85-933169754fcd");
        }
    }

    static Product buildProduct(Consumer<Product> customizer) {
        Product p = new Product();
        p.setProductId(UUID.randomUUID().toString());
        p.setProductName("BaseItem");
        p.setProductDescription("desc");
        p.setPriceExcl(1000);
        p.setStock(100);
        p.setStatus(SaleStatus.PUBLISHED);
        p.setVersion(1);
        p.setCreatedAt(LocalDateTime.of(2020, 1, 1, 12, 0));
        p.setUpdatedAt(LocalDateTime.of(2021, 6, 3, 12, 0));
        customizer.accept(p);
        return p;
    }

}
