package com.example.mapper.admin;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

import com.example.dto.admin.AdminDailyAggRow;
import com.example.dto.admin.AdminHourlyAggRow;
import com.example.entity.Order;
import com.example.enums.order.PaymentStatus;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
@Sql(scripts = {
        "/reset-order-data.sql"
})
class AdminDashboardMapperTest {

    @Autowired
    TestDataFactory factory;

    @Autowired
    AdminDashboardMapper adminDashboardMapper;

    @Nested
    class AggTodayHourly {

        static final LocalDateTime TIME_RANGE_START = LocalDateTime.of(2025, 8, 4, 0, 0, 0);

        static final LocalDateTime TIME_RANGE_END_EXCLUSIVE = LocalDateTime.of(2025, 8, 4, 17, 54, 20);

        @Test
        void aggTodayHourly_normal() {
            Order o1 = new Order();
            o1.setOrderId("o1");
            o1.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o1.setName("Tester");
            o1.setPostalCode("1000001");
            o1.setAddress("Tokyo");
            o1.setTotalQty(1);
            o1.setTotalPriceIncl(1000);
            o1.setPaymentStatus(PaymentStatus.PAID);
            o1.setCreatedAt(LocalDateTime.of(2025, 8, 4, 0, 0));
            o1.setUpdatedAt(LocalDateTime.of(2025, 8, 4, 0, 0));
            factory.createOrder(o1);

            Order o2 = new Order();
            o2.setOrderId("o2");
            o2.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o2.setName("Tester");
            o2.setPostalCode("1000001");
            o2.setAddress("Tokyo");
            o2.setTotalQty(1);
            o2.setTotalPriceIncl(1000);
            o2.setPaymentStatus(PaymentStatus.PAID);
            o2.setCreatedAt(LocalDateTime.of(2025, 8, 3, 23, 59, 59));
            o2.setUpdatedAt(LocalDateTime.of(2025, 8, 3, 23, 59, 59));
            factory.createOrder(o2);

            Order o3 = new Order();
            o3.setOrderId("o3");
            o3.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o3.setName("Tester");
            o3.setPostalCode("1000001");
            o3.setAddress("Tokyo");
            o3.setTotalQty(1);
            o3.setTotalPriceIncl(1000);
            o3.setPaymentStatus(PaymentStatus.PAID);
            o3.setCreatedAt(LocalDateTime.of(2025, 8, 4, 17, 54, 20));
            o3.setUpdatedAt(LocalDateTime.of(2025, 8, 4, 17, 54, 20));
            factory.createOrder(o3);

            Order o4 = new Order();
            o4.setOrderId("o4");
            o4.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o4.setName("Tester");
            o4.setPostalCode("1000001");
            o4.setAddress("Tokyo");
            o4.setTotalQty(1);
            o4.setTotalPriceIncl(1000);
            o4.setPaymentStatus(PaymentStatus.PAID);
            o4.setCreatedAt(LocalDateTime.of(2025, 8, 4, 17, 54, 19));
            o4.setUpdatedAt(LocalDateTime.of(2025, 8, 4, 17, 54, 19));
            factory.createOrder(o4);

            Order o5 = new Order();
            o5.setOrderId("o5");
            o5.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o5.setName("Tester");
            o5.setPostalCode("1000001");
            o5.setAddress("Tokyo");
            o5.setTotalQty(1);
            o5.setTotalPriceIncl(1000);
            o5.setPaymentStatus(PaymentStatus.PAID);
            o5.setCreatedAt(LocalDateTime.of(2025, 8, 4, 12, 0, 0));
            o5.setUpdatedAt(LocalDateTime.of(2025, 8, 4, 12, 0, 0));
            factory.createOrder(o5);

            Order o6 = new Order();
            o6.setOrderId("o6");
            o6.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o6.setName("Tester");
            o6.setPostalCode("1000001");
            o6.setAddress("Tokyo");
            o6.setTotalQty(1);
            o6.setTotalPriceIncl(1000);
            o6.setPaymentStatus(PaymentStatus.PAID);
            o6.setCreatedAt(LocalDateTime.of(2025, 8, 4, 12, 20, 10));
            o6.setUpdatedAt(LocalDateTime.of(2025, 8, 4, 12, 20, 10));
            factory.createOrder(o6);

            Order o7 = new Order();
            o7.setOrderId("o7");
            o7.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o7.setName("Tester");
            o7.setPostalCode("1000001");
            o7.setAddress("Tokyo");
            o7.setTotalQty(1);
            o7.setTotalPriceIncl(1000);
            o7.setPaymentStatus(PaymentStatus.UNPAID);
            o7.setCreatedAt(LocalDateTime.of(2025, 8, 4, 10, 5, 0));
            o7.setUpdatedAt(LocalDateTime.of(2025, 8, 4, 10, 5, 0));
            factory.createOrder(o7);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

            List<AdminHourlyAggRow> result = adminDashboardMapper.aggTodayHourly(TIME_RANGE_START,
                    TIME_RANGE_END_EXCLUSIVE);
            assertThat(result).extracting(AdminHourlyAggRow::getBucketHour)
                    .containsExactly(
                            LocalDateTime.of(2025, 8, 4, 0, 0).format(formatter),
                            LocalDateTime.of(2025, 8, 4, 12, 0, 0).format(formatter),
                            LocalDateTime.of(2025, 8, 4, 17, 0, 0).format(formatter));

            assertThat(result)
                    .filteredOn(r -> r.getBucketHour().equals(LocalDateTime.of(2025, 8, 4, 12, 0, 0).format(formatter)))
                    .singleElement()
                    .satisfies(r -> {
                        assertThat(r.getRevenue()).isEqualTo(2000);
                        assertThat(r.getOrders()).isEqualTo(2);
                    });
        }

        @ParameterizedTest
        @MethodSource("provideHourlyArguments")
        void aggTodayHourly_boundaries(LocalDateTime createdAt, boolean expected) {
            Order o1 = new Order();
            o1.setOrderId("o1");
            o1.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o1.setName("Tester");
            o1.setPostalCode("1000001");
            o1.setAddress("Tokyo");
            o1.setTotalQty(1);
            o1.setTotalPriceIncl(1000);
            o1.setPaymentStatus(PaymentStatus.PAID);
            o1.setCreatedAt(createdAt);
            o1.setUpdatedAt(createdAt);
            factory.createOrder(o1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

            List<AdminHourlyAggRow> result = adminDashboardMapper.aggTodayHourly(TIME_RANGE_START,
                    TIME_RANGE_END_EXCLUSIVE);

            boolean found = result.stream()
                    .anyMatch(r -> r.getBucketHour().equals(createdAt.format(formatter)));

            assertThat(found).isEqualTo(expected);
        }

        static Stream<Arguments> provideHourlyArguments() {
            return Stream.of(
                    Arguments.of(LocalDateTime.of(2025, 8, 4, 0, 0), true),
                    Arguments.of(LocalDateTime.of(2025, 8, 3, 23, 59, 59), false),
                    Arguments.of(LocalDateTime.of(2025, 8, 4, 17, 54, 20), false),
                    Arguments.of(LocalDateTime.of(2025, 8, 4, 17, 54, 19), true));
        }
    }

    @Nested
    class AggDaily {
        static final LocalDate DATE_RANGE_START = LocalDate.of(2025, 7, 20);

        static final LocalDate DATE_RANGE_END_EXCLUSIVE = LocalDate.of(2025, 7, 27);

        @BeforeEach
        void setup() {
            Order o1 = new Order();
            o1.setOrderId("o1");
            o1.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o1.setName("Tester");
            o1.setPostalCode("1000001");
            o1.setAddress("Tokyo");
            o1.setTotalQty(1);
            o1.setTotalPriceIncl(1000);
            o1.setPaymentStatus(PaymentStatus.PAID);
            o1.setCreatedAt(LocalDateTime.of(2025, 7, 20, 0, 0));
            o1.setUpdatedAt(LocalDateTime.of(2025, 7, 20, 0, 0));
            factory.createOrder(o1);

            Order o2 = new Order();
            o2.setOrderId("o2");
            o2.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o2.setName("Tester");
            o2.setPostalCode("1000001");
            o2.setAddress("Tokyo");
            o2.setTotalQty(1);
            o2.setTotalPriceIncl(1000);
            o2.setPaymentStatus(PaymentStatus.PAID);
            o2.setCreatedAt(LocalDateTime.of(2025, 7, 19, 23, 59, 59));
            o2.setUpdatedAt(LocalDateTime.of(2025, 7, 19, 23, 59, 59));
            factory.createOrder(o2);

            Order o3 = new Order();
            o3.setOrderId("o3");
            o3.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o3.setName("Tester");
            o3.setPostalCode("1000001");
            o3.setAddress("Tokyo");
            o3.setTotalQty(1);
            o3.setTotalPriceIncl(1000);
            o3.setPaymentStatus(PaymentStatus.PAID);
            o3.setCreatedAt(LocalDateTime.of(2025, 7, 27, 0, 0, 0));
            o3.setUpdatedAt(LocalDateTime.of(2025, 7, 27, 0, 0, 0));
            factory.createOrder(o3);

            Order o4 = new Order();
            o4.setOrderId("o4");
            o4.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o4.setName("Tester");
            o4.setPostalCode("1000001");
            o4.setAddress("Tokyo");
            o4.setTotalQty(1);
            o4.setTotalPriceIncl(1000);
            o4.setPaymentStatus(PaymentStatus.PAID);
            o4.setCreatedAt(LocalDateTime.of(2025, 7, 26, 23, 59, 59));
            o4.setUpdatedAt(LocalDateTime.of(2025, 7, 26, 23, 59, 59));
            factory.createOrder(o4);

            Order o5 = new Order();
            o5.setOrderId("o5");
            o5.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o5.setName("Tester");
            o5.setPostalCode("1000001");
            o5.setAddress("Tokyo");
            o5.setTotalQty(1);
            o5.setTotalPriceIncl(1000);
            o5.setPaymentStatus(PaymentStatus.PAID);
            o5.setCreatedAt(LocalDateTime.of(2025, 7, 23, 20, 52, 59));
            o5.setUpdatedAt(LocalDateTime.of(2025, 7, 23, 20, 52, 59));
            factory.createOrder(o5);

            Order o6 = new Order();
            o6.setOrderId("o6");
            o6.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o6.setName("Tester");
            o6.setPostalCode("1000001");
            o6.setAddress("Tokyo");
            o6.setTotalQty(1);
            o6.setTotalPriceIncl(1000);
            o6.setPaymentStatus(PaymentStatus.PAID);
            o6.setCreatedAt(LocalDateTime.of(2025, 7, 23, 20, 52, 59));
            o6.setUpdatedAt(LocalDateTime.of(2025, 7, 23, 20, 52, 59));
            factory.createOrder(o6);

            Order o7 = new Order();
            o7.setOrderId("o7");
            o7.setUserId("550e8400-e29b-41d4-a716-446655440000");
            o7.setName("Tester");
            o7.setPostalCode("1000001");
            o7.setAddress("Tokyo");
            o7.setTotalQty(1);
            o7.setTotalPriceIncl(1000);
            o7.setPaymentStatus(PaymentStatus.UNPAID);
            o7.setCreatedAt(LocalDateTime.of(2025, 7, 24, 10, 59, 59));
            o7.setUpdatedAt(LocalDateTime.of(2025, 7, 24, 10, 59, 59));
            factory.createOrder(o7);
        }

        @Test
        void aggDaily_normal() {
            List<AdminDailyAggRow> result = adminDashboardMapper.aggDaily(DATE_RANGE_START, DATE_RANGE_END_EXCLUSIVE);
            assertThat(result).extracting(AdminDailyAggRow::getDay)
                    .containsExactly(
                            LocalDate.of(2025, 7, 20),
                            LocalDate.of(2025, 7, 23),
                            LocalDate.of(2025, 7, 26));

            assertThat(result)
                    .filteredOn(r -> r.getDay().equals(LocalDate.of(2025, 7, 23)))
                    .singleElement()
                    .satisfies(r -> {
                        assertThat(r.getRevenue()).isEqualTo(2000);
                        assertThat(r.getOrders()).isEqualTo(2);
                    });
        }

        @ParameterizedTest
        @MethodSource("provideDailyArguments")
        void aggDaily_boundaries(LocalDateTime createdAt, boolean expected) {
            List<AdminDailyAggRow> result = adminDashboardMapper.aggDaily(DATE_RANGE_START, DATE_RANGE_END_EXCLUSIVE);

            boolean found = result.stream()
                    .anyMatch(r -> r.getDay().equals(createdAt.toLocalDate()));

            assertThat(found).isEqualTo(expected);
        }

        static Stream<Arguments> provideDailyArguments() {
            return Stream.of(
                    Arguments.of(LocalDateTime.of(2025, 7, 20, 0, 0), true),
                    Arguments.of(LocalDateTime.of(2025, 7, 19, 23, 59, 59), false),
                    Arguments.of(LocalDateTime.of(2025, 7, 27, 0, 0, 0), false),
                    Arguments.of(LocalDateTime.of(2025, 7, 26, 23, 59, 59), true));
        }

    }

}
