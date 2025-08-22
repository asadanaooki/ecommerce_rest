package com.example.mapper.admin;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.example.dto.admin.AdminInventoryDto;
import com.example.entity.Product;
import com.example.enums.InventorySortField;
import com.example.enums.SaleStatus;
import com.example.enums.SortDirection;
import com.example.enums.StockStatus;
import com.example.request.admin.InventorySearchRequest;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
@Sql(scripts = {
        "/cleanup.sql"
})
class AdminInventoryMapperTest {

    final int THRESHOLD = 10;

    @Autowired
    AdminInventoryMapper adminInventoryMapper;

    @Autowired
    TestDataFactory factory;

    @BeforeEach
    void setup() {
        // Product 1: OUT_OF_STOCK (available = 0)
        Product p1 = new Product();
        p1.setProductId("p1");
        p1.setProductName("Item1");
        p1.setSku(1);
        p1.setPriceExcl(1200);
        p1.setStock(0);
        p1.setReserved(0);
        p1.setStatus(SaleStatus.PUBLISHED);
        factory.createProduct(p1);

        // Product 2: LOW_STOCK (available = 9)
        Product p2 = new Product();
        p2.setProductId("p2");
        p2.setProductName("Item2");
        p2.setSku(2);
        p2.setPriceExcl(1000);
        p2.setStock(10);
        p2.setReserved(1);
        p2.setStatus(SaleStatus.PUBLISHED);
        factory.createProduct(p2);

        // Product 3: LOW_STOCK boundary (available = 10)
        Product p3 = new Product();
        p3.setProductId("p3");
        p3.setProductName("Item3");
        p3.setSku(3);
        p3.setPriceExcl(2000);
        p3.setStock(10);
        p3.setReserved(0);
        p3.setStatus(SaleStatus.PUBLISHED);
        factory.createProduct(p3);

        // Product 4: IN_STOCK (available = 11)
        Product p4 = new Product();
        p4.setProductId("p4");
        p4.setProductName("Item4");
        p4.setSku(4);
        p4.setPriceExcl(1000);
        p4.setStock(11);
        p4.setReserved(0);
        p4.setStatus(SaleStatus.PUBLISHED);
        factory.createProduct(p4);

        // Product 5: IN_STOCK (available = 15)
        Product p5 = new Product();
        p5.setProductId("p5");
        p5.setProductName("Item5");
        p5.setSku(5);
        p5.setPriceExcl(3000);
        p5.setStock(15);
        p5.setReserved(0);
        p5.setStatus(SaleStatus.PUBLISHED);
        factory.createProduct(p5);
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void search(InventorySearchRequest req, List<String> expected) {
        List<AdminInventoryDto> list = adminInventoryMapper.search(req, THRESHOLD, 100, 0);

        assertThat(list).extracting(AdminInventoryDto::getProductId)
                .containsExactlyElementsOf(expected);

    }

    static Stream<Arguments> provideArguments() {
        return Stream.of(
                // フィルター
                // minAvailable
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setMinAvailable(12);
                            }
                        },
                        List.of("p5")),
                // maxAvailable
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setMaxAvailable(13);
                            }
                        },
                        List.of("p1", "p2", "p3", "p4")),
                // stockStatus OUT_OF_STOCK
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setStockStatus(StockStatus.OUT_OF_STOCK);
                            }
                        },
                        List.of("p1")),
                // stockStatus LOW_STOCK 閾値の境界も同時に検証
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setStockStatus(StockStatus.LOW_STOCK);
                            }
                        },
                        List.of("p2", "p3")),
                // stockStatus IN_STOCK 閾値の境界も同時に検証
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setStockStatus(StockStatus.IN_STOCK);
                            }
                        },
                        List.of("p4", "p5")),
                // 複合フィルタ
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setMinAvailable(13);
                                setMaxAvailable(20);
                            }
                        },
                        List.of("p5")),

                // 境界値
                // minAvailable
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setMinAvailable(10);
                            }
                        },
                        List.of("p3", "p4", "p5")),
                // maxAvailable
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setMaxAvailable(10);
                            }
                        },
                        List.of("p1", "p2", "p3")),

                // ソート
                // stockStatus 二次ソートも同時検証
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setSortField(InventorySortField.STATUS);
                                setSortDirection(SortDirection.ASC);
                            }
                        },
                        List.of("p1", "p2", "p3", "p4", "p5")),
                // stockStatus 二次ソートも同時検証
                Arguments.of(
                        new InventorySearchRequest() {
                            {
                                setSortField(InventorySortField.PRICE);
                                setSortDirection(SortDirection.DESC);
                            }
                        },
                        List.of("p5", "p3", "p1", "p2", "p4")));

    }

}
