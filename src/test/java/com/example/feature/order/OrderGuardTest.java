package com.example.feature.order;

import static org.assertj.core.api.Assertions.*;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.enums.order.OrderEvent;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.mapper.OrderMapper;

@ExtendWith(MockitoExtension.class)
class OrderGuardTest {

    @Mock
    OrderMapper orderMapper;

    @InjectMocks
    OrderGuard sut;

    // ===== Terminal 判定 =====
    static final EnumSet<OrderStatus> TERMINAL_ORDER_STATUS = EnumSet.of(OrderStatus.CANCELED, OrderStatus.COMPLETED);

    static boolean isTerminal(OrderStatus o) {
        return TERMINAL_ORDER_STATUS.contains(o);
    }

    // ===== イベント別: 許可ホワイトリスト（from 条件） =====
    static boolean allowedRequestCancel(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        return o == OrderStatus.OPEN &&
                s == ShippingStatus.UNSHIPPED &&
                p == PaymentStatus.UNPAID;
    }

    static boolean allowedApproveCancel(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        return o == OrderStatus.CANCEL_REQUESTED &&
                s == ShippingStatus.UNSHIPPED &&
                p == PaymentStatus.UNPAID;
    }

    static boolean allowedShip(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        return (o == OrderStatus.OPEN || o == OrderStatus.CANCEL_REQUESTED) &&
                s == ShippingStatus.UNSHIPPED &&
                p == PaymentStatus.UNPAID;
    }

    static boolean allowedDelivered(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        return o == OrderStatus.OPEN &&
                s == ShippingStatus.SHIPPED &&
                p == PaymentStatus.UNPAID;
    }

    // ===== 非終端 NG メッセージ =====
    static final Map<OrderEvent, String> NG_MESSAGE = Map.of(
            OrderEvent.REQUEST_CANCEL, "Cancel request not allowed",
            OrderEvent.APPROVE_CANCEL, "Approve not allowed",
            OrderEvent.SHIP, "Ship not allowed",
            OrderEvent.DELIVERED, "Delivered not allowed");

    // ===== 全状態列挙 =====
    static Stream<Arguments> allStates() {
        return Stream.of(OrderStatus.values())
                .flatMap(o -> Stream.of(ShippingStatus.values())
                        .flatMap(s -> Stream.of(PaymentStatus.values())
                                .map(p -> Arguments.of(o, s, p))));
    }

    @ParameterizedTest
    @MethodSource("allStates")
    void next_requestCancel(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        OrderState cur = new OrderState(o, s, p);

        if (isTerminal(o)) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.REQUEST_CANCEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Terminal state");
            return;
        }

        if (allowedRequestCancel(o, s, p)) {
            OrderState next = sut.next(cur, OrderEvent.REQUEST_CANCEL);
            assertThat(next.getOrder()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.UNSHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        } else {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.REQUEST_CANCEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(NG_MESSAGE.get(OrderEvent.REQUEST_CANCEL));
        }
    }

    @ParameterizedTest
    @MethodSource("allStates")
    void next_approveCancel(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        OrderState cur = new OrderState(o, s, p);

        if (isTerminal(o)) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.APPROVE_CANCEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Terminal state");
            return;
        }

        if (allowedApproveCancel(o, s, p)) {
            OrderState next = sut.next(cur, OrderEvent.APPROVE_CANCEL);
            assertThat(next.getOrder()).isEqualTo(OrderStatus.CANCELED);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.UNSHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        } else {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.APPROVE_CANCEL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(NG_MESSAGE.get(OrderEvent.APPROVE_CANCEL));
        }
    }

    @ParameterizedTest
    @MethodSource("allStates")
    void next_ship(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        OrderState cur = new OrderState(o, s, p);

        if (isTerminal(o)) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.SHIP))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Terminal state");
            return;
        }

        if (allowedShip(o, s, p)) {
            OrderState next = sut.next(cur, OrderEvent.SHIP);
            assertThat(next.getOrder()).isEqualTo(OrderStatus.OPEN);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.SHIPPED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.UNPAID);
        } else {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.SHIP))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(NG_MESSAGE.get(OrderEvent.SHIP));
        }
    }

    @ParameterizedTest
    @MethodSource("allStates")
    void next_delivered(OrderStatus o, ShippingStatus s, PaymentStatus p) {
        OrderState cur = new OrderState(o, s, p);

        if (isTerminal(o)) {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.DELIVERED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Terminal state");
            return;
        }

        if (allowedDelivered(o, s, p)) {
            OrderState next = sut.next(cur, OrderEvent.DELIVERED);
            assertThat(next.getOrder()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(next.getShipping()).isEqualTo(ShippingStatus.DELIVERED);
            assertThat(next.getPayment()).isEqualTo(PaymentStatus.PAID);
        } else {
            assertThatThrownBy(() -> sut.next(cur, OrderEvent.DELIVERED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(NG_MESSAGE.get(OrderEvent.DELIVERED));
        }
    }


}
