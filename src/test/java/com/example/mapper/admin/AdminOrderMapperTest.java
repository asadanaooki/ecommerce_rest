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

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;
import com.example.mapper.CheckoutMapper;
import com.example.request.admin.OrderSearchRequest;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
@Sql(scripts = {
        "/reset-order-data.sql"
})
class AdminOrderMapperTest {

    @Autowired
    AdminOrderMapper adminOrderMapper;

    @Autowired
    CheckoutMapper checkoutMapper;

    @Autowired
    TestDataFactory factory;

    @Nested
    class Count {
        @BeforeEach
        void setup() {
            factory.createOrder(buildOrder(o -> {
            }));
        }

        @ParameterizedTest
        @MethodSource("provideSingleFilterAndBoundaryCases")
        void count_singleFilterAndBoundary(Consumer<TestDataFactory> insertMismatch,
                Consumer<OrderSearchRequest> customizeReq, int expected) {
            insertMismatch.accept(factory);

            OrderSearchRequest req = new OrderSearchRequest();
            customizeReq.accept(req);

            assertThat(adminOrderMapper.count(req)).isEqualTo(expected);
        }

        static Stream<Arguments> provideSingleFilterAndBoundaryCases() {
            String userId = "111e8400-e29b-41d4-a716-446655440111";
            return Stream.of(
                    // フィルタなし
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(buildOrder(o -> {
                                o.setUserId(userId);
                            })),
                            (Consumer<OrderSearchRequest>) o -> {
                            },
                            2),
                    // keyword
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(buildOrder(o -> {
                                o.setUserId(userId);
                                o.setName("笠谷 花子");
                            })),
                            (Consumer<OrderSearchRequest>) o -> {
                                o.setQ("笠谷");
                            },
                            1),
                    // shippingStatus
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(buildOrder(o -> {
                                o.setUserId(userId);
                                o.setShippingStatus(ShippingStatus.SHIPPED);
                            })),
                            (Consumer<OrderSearchRequest>) o -> {
                                o.setShippingStatus(ShippingStatus.SHIPPED);
                            },
                            1),
                    // paymentStatus
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(buildOrder(o -> {
                                o.setUserId(userId);
                                o.setPaymentStatus(PaymentStatus.PAID);
                            })),
                            (Consumer<OrderSearchRequest>) o -> {
                                o.setPaymentStatus(PaymentStatus.PAID);
                            },
                            1),
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(
                                    buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2018, 8, 2, 10, 44, 3)))),
                            (Consumer<OrderSearchRequest>) o -> o.setCreatedFrom(LocalDate.of(2019, 3, 3)),
                            1),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(
                                    buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2025, 7, 2, 10, 44, 3)))),
                            (Consumer<OrderSearchRequest>) o -> o.setCreatedTo(LocalDate.of(2022, 4, 1)),
                            1),

                    // 境界値
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2019, 12, 31, 1, 2, 3)))),
                            (Consumer<OrderSearchRequest>) o -> o.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2019, 12, 31, 1, 2, 3)))),
                            (Consumer<OrderSearchRequest>) o -> o.setCreatedFrom(LocalDate.of(2019, 12, 31)),
                            2),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2020, 1, 2, 1, 4, 3)))),
                            (Consumer<OrderSearchRequest>) o -> o.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2020, 1, 2, 1, 4, 3)))),
                            (Consumer<OrderSearchRequest>) o -> o.setCreatedTo(LocalDate.of(2020, 1, 2)),
                            2));
        }

        @ParameterizedTest
        @MethodSource("provideKeywordCases")
        void countProducts_keywordFilter(
                Consumer<TestDataFactory> insertMatching,
                Consumer<OrderSearchRequest> customizeReq,
                int expected) {
            insertMatching.accept(factory);

            OrderSearchRequest req = new OrderSearchRequest();
            customizeReq.accept(req);

            assertThat(adminOrderMapper.count(req)).isEqualTo(expected);
        }

        static Stream<Arguments> provideKeywordCases() {
            return Stream.of(
                    // name
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(buildOrder(o -> o.setName("笠谷 花子"))),
                            (Consumer<OrderSearchRequest>) o -> o.setQ("笠谷花"),
                            1),
                    // orderNumber
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(buildOrder(o -> o.setOrderNumber(2000))),
                            (Consumer<OrderSearchRequest>) r -> r.setQ("200"),
                            1),
                    // 不一致
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(buildOrder(o -> o.setName("NMH"))),
                            (Consumer<OrderSearchRequest>) r -> r.setQ("nbs"),
                            0));
        }
    }

    @Test
    void selectOrderHeader() {
        String orderId = "a12f3e45-6789-4abc-de01-23456789abcd";
        factory.createOrder(buildOrder(o -> {
            o.setOrderId(orderId);
            o.setOrderNumber(200);
        }));

        AdminOrderDetailDto dto = adminOrderMapper.selectOrderHeader(orderId);

        assertThat(dto).extracting(
                AdminOrderDetailDto::getOrderId,
                AdminOrderDetailDto::getOrderNumber,
                AdminOrderDetailDto::getTotalPrice,
                AdminOrderDetailDto::getShippingStatus,
                AdminOrderDetailDto::getPaymentStatus,
                AdminOrderDetailDto::getCreatedAt,

                AdminOrderDetailDto::getItems,

                AdminOrderDetailDto::getName,
                AdminOrderDetailDto::getNameKana,
                AdminOrderDetailDto::getEmail,
                AdminOrderDetailDto::getPostalCode,
                AdminOrderDetailDto::getAddress,
                AdminOrderDetailDto::getPhoneNumber)
                .containsExactly(
                        orderId,
                        "0200",
                        3000,
                        ShippingStatus.NOT_SHIPPED,
                        PaymentStatus.UNPAID,
                        LocalDateTime.of(2020, 1, 1, 10, 3, 4),

                        null,

                        "山田 太郎",
                        "ヤマダ タロウ",
                        "sample@example.com",
                        "1500041",
                        "test",
                        "0312345678");
    }

    @Test
    void updateTotals() {
        String orderId = "a12f3e45-6789-4abc-de01-23456789abcd";
        factory.createOrder(buildOrder(o -> {
            o.setOrderId(orderId);
        }));
        checkoutMapper.insertOrderItems(List.of(new OrderItem() {
            {
                setOrderId(orderId);
                setProductId("97113c2c-719a-490c-9979-144d92905c33");
                setProductName("test");
                setQty(2);
                setPrice(1000);
                setSubtotal(2000);
            }
        }));
        checkoutMapper.insertOrderItems(List.of(new OrderItem() {
            {
                setOrderId(orderId);
                setProductId("09d5a43a-d24c-41c7-af2b-9fb7b0c9e049");
                setProductName("test2");
                setQty(1);
                setPrice(1000);
                setSubtotal(1000);
            }
        }));

        adminOrderMapper.updateItemQty(new OrderItem() {
            {
                setOrderId(orderId);
                setProductId("97113c2c-719a-490c-9979-144d92905c33");
                setQty(1);
                setSubtotal(1000);
            }
        });

        adminOrderMapper.updateTotals(orderId);
        Order order = checkoutMapper.selectOrderByPrimaryKey(orderId);
        
        assertThat(order.getTotalQty()).isEqualTo(2);
        assertThat(order.getTotalPrice()).isEqualTo(2000);

    }

    static Order buildOrder(Consumer<Order> customizer) {
        Order o = new Order();
        o.setOrderId(UUID.randomUUID().toString());
        o.setUserId("550e8400-e29b-41d4-a716-446655440000");
        o.setName("山田 太郎");
        o.setPostalCode("1500041");
        o.setAddress("test");
        o.setTotalQty(3);
        o.setTotalPrice(3000);
        o.setCreatedAt(LocalDateTime.of(2020, 1, 1, 10, 3, 4));
        o.setUpdatedAt(LocalDateTime.of(2021, 6, 3, 10, 40, 5));
        customizer.accept(o);
        return o;
    }

}
