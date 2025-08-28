package com.example.feature.order;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.enums.order.OrderEvent;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;

class OrderTransitionGuardTest {

    OrderTransitionGuard sut = new OrderTransitionGuard();

    @Nested
    class RequestCancel {
        @Test
        void allowed() {
            OrderState cur = new OrderState(OrderStatus.OPEN,
                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID);

            OrderState next = sut.next(cur, OrderEvent.REQUEST_CANCEL);

            assertThat(next.getOrder()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.UNSHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        }

        @ParameterizedTest(name = "[REQUEST_CANCEL] {0}")
        @MethodSource("notAllowedCases")
        void notAllowed(String label, OrderState cur, String expectedMsg) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.REQUEST_CANCEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMsg);
        }

        static Stream<Arguments> notAllowedCases() {
            return Stream.of(
                    Arguments.of("already requested",
                            new OrderState(OrderStatus.CANCEL_REQUESTED,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID),
                            "Cancel request not allowed"),
                    Arguments.of("shipped",
                            new OrderState(OrderStatus.CANCEL_REQUESTED,
                                    ShippingStatus.SHIPPED, PaymentStatus.UNPAID),
                            "Cancel request not allowed"),
                    Arguments.of("paid",
                            new OrderState(OrderStatus.OPEN,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.PAID),
                            "Cancel request not allowed"));
        }
    }

    @Nested
    class ApproveCancel {
        @Test
        void allowed() {
            OrderState cur = new OrderState(OrderStatus.CANCEL_REQUESTED,
                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID);

            OrderState next = sut.next(cur, OrderEvent.APPROVE_CANCEL);

            assertThat(next.getOrder()).isEqualTo(OrderStatus.CANCELED);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.UNSHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("notAllowedCases")
        void notAllowed(String label, OrderState cur, String expectedMsg) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.APPROVE_CANCEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMsg);
        }

        static Stream<Arguments> notAllowedCases() {
            return Stream.of(
                    Arguments.of("not requested",
                            new OrderState(OrderStatus.OPEN,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID),
                            "Approve not allowed"),
                    Arguments.of("shipped",
                            new OrderState(OrderStatus.CANCEL_REQUESTED,
                                    ShippingStatus.SHIPPED, PaymentStatus.UNPAID),
                            "Approve not allowed"),
                    Arguments.of("paid",
                            new OrderState(OrderStatus.CANCEL_REQUESTED,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.PAID),
                            "Approve not allowed"));
        }
    }

    @Nested
    class Ship {
        @Test
        void allowed_fromOpen() {
            OrderState cur = new OrderState(OrderStatus.OPEN,
                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID);

            OrderState next = sut.next(cur, OrderEvent.SHIP);

            assertThat(next.getOrder()).isEqualTo(OrderStatus.OPEN);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.SHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        }

        @Test
        void allowed_fromCancelRequested() {
            OrderState cur = new OrderState(OrderStatus.CANCEL_REQUESTED,
                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID);

            OrderState next = sut.next(cur, OrderEvent.SHIP);

            assertThat(next.getOrder()).isEqualTo(OrderStatus.OPEN);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.SHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("notAllowedCases")
        void notAllowed(String label, OrderState cur, String expectedMsg) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.SHIP))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMsg);
        }

        static Stream<Arguments> notAllowedCases() {
            return Stream.of(
                    Arguments.of("already shipped",
                            new OrderState(OrderStatus.OPEN,
                                    ShippingStatus.SHIPPED, PaymentStatus.UNPAID),
                            "Ship not allowed"),
                    Arguments.of("paid",
                            new OrderState(OrderStatus.OPEN,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.PAID),
                            "Ship not allowed"));
        }
    }

    @Nested
    class Delivered {
        @Test
        void allowed() {
            OrderState cur = new OrderState(OrderStatus.OPEN,
                    ShippingStatus.SHIPPED, PaymentStatus.UNPAID);

            OrderState next = sut.next(cur, OrderEvent.DELIVERED);

            assertThat(next.getOrder()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.DELIVERED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.PAID);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("notAllowedCases")
        void notAllowed(String label, OrderState cur, String expectedMsg) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.DELIVERED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMsg);
        }

        static Stream<Arguments> notAllowedCases() {
            return Stream.of(
                    Arguments.of("cancel requested",
                            new OrderState(OrderStatus.CANCEL_REQUESTED,
                                    ShippingStatus.SHIPPED, PaymentStatus.UNPAID),
                            "Delivered not allowed"),
                    Arguments.of("not shipped",
                            new OrderState(OrderStatus.OPEN,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID),
                            "Delivered not allowed"),
                    Arguments.of("paid",
                            new OrderState(OrderStatus.OPEN,
                                    ShippingStatus.SHIPPED, PaymentStatus.PAID),
                            "Delivered not allowed"));
        }
    }

    @Nested
    class TerminalGuard {

        @ParameterizedTest
        @MethodSource("terminalCases")
        void notAllowed(String label, OrderState cur, OrderEvent ev) {
            assertThatThrownBy(() -> sut.next(cur, ev))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Terminal state");
        }

        static Stream<Arguments> terminalCases() {
            return Stream.of(
                    Arguments.of("canceled",
                            new OrderState(OrderStatus.CANCELED,
                                    ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID),
                            OrderEvent.REQUEST_CANCEL),
                    Arguments.of("completed",
                            new OrderState(OrderStatus.COMPLETED,
                                    ShippingStatus.DELIVERED, PaymentStatus.PAID),
                            OrderEvent.SHIP));

        }
    }

}
