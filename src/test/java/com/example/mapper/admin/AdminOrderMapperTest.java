package com.example.mapper.admin;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.mapper.OrderMapper;
import com.example.request.admin.AdminOrderSearchRequest;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.OrderTestFactory;
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
    OrderMapper orderMapper;

    @Autowired
    TestDataFactory factory;


    @Nested
    class Count {
        @Autowired
        TestDataFactory factory;

        @BeforeEach
        void setup() {
            factory.createOrder(OrderTestFactory.buildOrder(o -> {
            }));
        }

        @ParameterizedTest
        @MethodSource("provideSingleFilterAndBoundaryCases")
        void count_singleFilterAndBoundary(Consumer<TestDataFactory> insertMismatch,
                Consumer<AdminOrderSearchRequest> customizeReq, int expected) {
            insertMismatch.accept(factory);

            AdminOrderSearchRequest req = new AdminOrderSearchRequest();
            customizeReq.accept(req);
            int a = adminOrderMapper.count(req);

            assertThat(a).isEqualTo(expected);
        }

        static Stream<Arguments> provideSingleFilterAndBoundaryCases() {
            return Stream.of(
                    // フィルタなし
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(OrderTestFactory.buildOrder(o -> {
                            })),
                            (Consumer<AdminOrderSearchRequest>) o -> {
                            },
                            2),
                    // keyword
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(OrderTestFactory.buildOrder(o -> {
                                o.setName("笠谷 花子");
                            })),
                            (Consumer<AdminOrderSearchRequest>) o -> {
                                o.setQ("笠谷");
                            },
                            1),
                    // orderStatus
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(OrderTestFactory.buildOrder(o -> {
                                o.setOrderStatus(OrderStatus.COMPLETED);
                            })),
                            (Consumer<AdminOrderSearchRequest>) o -> {
                                o.setOrderStatus(OrderStatus.COMPLETED);
                            },
                            1),
                    // paymentStatus
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(OrderTestFactory.buildOrder(o -> {
                                o.setPaymentStatus(PaymentStatus.PAID);
                            })),
                            (Consumer<AdminOrderSearchRequest>) o -> {
                                o.setPaymentStatus(PaymentStatus.PAID);
                            },
                            1),
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(
                                    OrderTestFactory
                                            .buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2018, 1, 1, 10, 3, 4)))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setCreatedFrom(LocalDate.of(2019, 3, 3)),
                            1),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f.createOrder(
                                    OrderTestFactory
                                            .buildOrder(o -> o.setCreatedAt(LocalDateTime.of(2022, 4, 2, 10, 3, 4)))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setCreatedTo(LocalDate.of(2022, 4, 1)),
                            1),

                    // 境界値
                    // createdFrom
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            OrderTestFactory.buildOrder(
                                                    o -> o.setCreatedAt(LocalDateTime.of(2019, 12, 31, 23, 59, 59)))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            OrderTestFactory.buildOrder(
                                                    o -> o.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0, 0)))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setCreatedFrom(LocalDate.of(2020, 1, 1)),
                            2),
                    // createdTo
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            OrderTestFactory.buildOrder(
                                                    o -> o.setCreatedAt(LocalDateTime.of(2020, 1, 2, 0, 0, 0)))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            1),
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(
                                            OrderTestFactory.buildOrder(
                                                    o -> o.setCreatedAt(LocalDateTime.of(2020, 1, 1, 23, 59, 59)))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setCreatedTo(LocalDate.of(2020, 1, 1)),
                            2));
        }

        @ParameterizedTest
        @MethodSource("provideKeywordCases")
        void countProducts_keywordFilter(
                Consumer<TestDataFactory> insertMatching,
                Consumer<AdminOrderSearchRequest> customizeReq,
                int expected) {
            insertMatching.accept(factory);

            AdminOrderSearchRequest req = new AdminOrderSearchRequest();
            customizeReq.accept(req);

            assertThat(adminOrderMapper.count(req)).isEqualTo(expected);
        }

        static Stream<Arguments> provideKeywordCases() {
            return Stream.of(
                    // name
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(OrderTestFactory.buildOrder(o -> o.setName("笠谷 花子"))),
                            (Consumer<AdminOrderSearchRequest>) o -> o.setQ("笠谷花"),
                            1),
                    // orderNumber
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(OrderTestFactory.buildOrder(o -> o.setOrderNumber(2000))),
                            (Consumer<AdminOrderSearchRequest>) r -> r.setQ("200"),
                            1),
                    // 不一致
                    Arguments.of(
                            (Consumer<TestDataFactory>) f -> f
                                    .createOrder(OrderTestFactory.buildOrder(o -> o.setName("NMH"))),
                            (Consumer<AdminOrderSearchRequest>) r -> r.setQ("nbs"),
                            0));
        }
    }

    @Test
    void selectOrderDetail() {
        String orderId = "a12f3e45-6789-4abc-de01-23456789abcd";
        factory.createOrder(OrderTestFactory.buildOrder(o -> {
            o.setName("abc");
            o.setPostalCode("4500311");
            o.setOrderId(orderId);
            o.setOrderNumber(200);
        }));
        OrderItem i1 = new OrderItem();
        i1.setOrderId(orderId);
        i1.setProductId("09d5a43a-d24c-41c7-af2b-9fb7b0c9e049");
        i1.setProductName("test");
        i1.setQty(3);
        i1.setUnitPriceIncl(220);

        OrderItem i2 = new OrderItem();
        i2.setOrderId(orderId);
        i2.setProductId("6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd");
        i2.setProductName("test2");
        i2.setQty(1);
        i2.setUnitPriceIncl(100);

        orderMapper.insertOrderItems(List.of(i1, i2));
        AdminOrderDetailDto dto = adminOrderMapper.selectOrderDetail(orderId);

        assertThat(dto).extracting(
                AdminOrderDetailDto::getOrderId,
                AdminOrderDetailDto::getOrderNumber,
                AdminOrderDetailDto::getItemsSubtotalIncl,
                AdminOrderDetailDto::getShippingFeeIncl,
                AdminOrderDetailDto::getCodFeeIncl,
                AdminOrderDetailDto::getGrandTotalIncl,
                AdminOrderDetailDto::getOrderStatus,
                AdminOrderDetailDto::getShippingStatus,
                AdminOrderDetailDto::getPaymentStatus,
                AdminOrderDetailDto::getCreatedAt,

                AdminOrderDetailDto::getName,
                AdminOrderDetailDto::getEmail,
                AdminOrderDetailDto::getPostalCode,
                AdminOrderDetailDto::getAddress,
                AdminOrderDetailDto::getPhoneNumber)
                .containsExactly(
                        orderId,
                        "0200",
                        4000,
                        500,
                        330,
                        4830,
                        OrderStatus.OPEN,
                        ShippingStatus.UNSHIPPED,
                        PaymentStatus.UNPAID,
                        LocalDateTime.of(2020, 1, 1, 10, 3, 4),

                        "abc",
                        "sample@example.com",
                        "4500311",
                        "test",
                        "0312345678");

        assertThat(dto.getItems()).hasSize(2);
        AdminOrderDetailItemDto d1 = dto.getItems().get(0);
        assertThat(d1.getProductId()).isEqualTo("09d5a43a-d24c-41c7-af2b-9fb7b0c9e049");
        assertThat(d1.getSku()).isEqualTo("0002");
        assertThat(d1.getProductName()).isEqualTo("test");
        assertThat(d1.getQty()).isEqualTo(3);
        assertThat(d1.getUnitPriceIncl()).isEqualTo(220);
        assertThat(d1.getSubtotalIncl()).isEqualTo(660);

        AdminOrderDetailItemDto d2 = dto.getItems().get(1);
        assertThat(d2.getProductId()).isEqualTo("6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd");
        assertThat(d2.getSku()).isEqualTo("0003");
    }

    @Test
    void updateTotals() {
        String orderId = "a12f3e45-6789-4abc-de01-23456789abcd";
        factory.createOrder(OrderTestFactory.buildOrder(o -> {
            o.setOrderId(orderId);
        }));

        int updated = orderMapper.updateTotals(orderId, 2000, 0);

        Order order = orderMapper.selectOrderByPrimaryKey(orderId);

        assertThat(order.getTotalQty()).isEqualTo(2);
        assertThat(order.getItemsSubtotalIncl()).isEqualTo(2000);
        assertThat(order.getShippingFeeIncl()).isEqualTo(0);
        assertThat(order.getCodFeeIncl()).isEqualTo(330);
        assertThat(order.getGrandTotalIncl()).isEqualTo(2330);

    }
    
    @Nested
    class SelectMonthlySalesTotal {
        LocalDateTime start = LocalDateTime.of(2025, 8, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 9, 1, 0, 0);

        @ParameterizedTest
        @MethodSource("providePeriodCases")
        void SelectMonthlySalesTotal_boundary(Consumer<Order> customizer, int expected) {
            LocalDateTime base = LocalDateTime.of(2025, 8, 15, 12, 0);
            factory.createOrder(OrderTestFactory.buildOrder(o -> o.setShippedAt(base)));

            Order order = OrderTestFactory.buildOrder(customizer);
            factory.createOrder(order);


            int actual = adminOrderMapper.selectMonthlySalesTotal(start, end);

            assertThat(actual).isEqualTo(expected);
        }

        static Stream<Arguments> providePeriodCases() {

            return Stream.of(
                    Arguments.of((Consumer<Order>) o -> o.setShippedAt(
                            LocalDateTime.of(2025, 7, 31, 23, 59, 59)), 4830),

                    Arguments.of((Consumer<Order>) o -> {
                        o.setShippedAt(LocalDateTime.of(2025, 8, 1, 0, 0));
                        o.setItemsSubtotalIncl(5000);
                    }, 10660),

                    Arguments.of((Consumer<Order>) o -> {
                        o.setShippedAt(LocalDateTime.of(2025, 8, 31, 23, 59, 59));
                        o.setItemsSubtotalIncl(5000);
                    }, 10660),

                    Arguments.of((Consumer<Order>) o -> {
                        o.setShippedAt(LocalDateTime.of(2025, 9, 1, 0, 0));
                        o.setItemsSubtotalIncl(5000);
                    }, 4830));
        }
        
        @Test
        void selectMonthlySalesTotal_noData() {
            Order o1 = OrderTestFactory.buildOrder(o ->{
                o.setShippedAt(null);
                o.setCreatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
                o.setUpdatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
            });
            factory.createOrder(o1);
            
            Order o2 = OrderTestFactory.buildOrder(o ->{
                o.setShippedAt(LocalDateTime.of(2025, 6, 10, 12, 0));
                o.setCreatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
                o.setUpdatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
            });
            factory.createOrder(o2);
            
            int actual = adminOrderMapper.selectMonthlySalesTotal(start, end);
            assertThat(actual).isZero();
        }
        
        @Test
        void selectMonthlySalesTotal_multiple() {
            Order o1 = OrderTestFactory.buildOrder(o ->{
                o.setShippedAt(null);
                o.setCreatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
                o.setUpdatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
            });
            factory.createOrder(o1);
            
            Order o2 = OrderTestFactory.buildOrder(o ->{
                o.setShippedAt(LocalDateTime.of(2025, 6, 10, 12, 0));
                o.setCreatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
                o.setUpdatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
            });
            factory.createOrder(o2);
            
            Order o3 = OrderTestFactory.buildOrder(o ->{
                o.setShippingFeeIncl(0);
                o.setItemsSubtotalIncl(500);
                o.setShippedAt(LocalDateTime.of(2025, 8, 24, 12, 0));
                o.setCreatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
                o.setUpdatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
            });
            factory.createOrder(o3);
            
            Order o4 = OrderTestFactory.buildOrder(o ->{
                o.setShippedAt(LocalDateTime.of(2025, 8, 2, 12, 0));
                o.setCreatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
                o.setUpdatedAt(LocalDateTime.of(2025, 8, 10, 12, 0));
            });
            factory.createOrder(o4);
            
            int actual = adminOrderMapper.selectMonthlySalesTotal(start, end);
            assertThat(actual).isEqualTo(5660);
        }
    }

}
