package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.order.OrderEvent;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.feature.order.OrderState;
import com.example.feature.order.OrderGuard;
import com.example.mapper.IdempotencyMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.UserMapper;
import com.example.support.IdempotentExecutor;
import com.example.support.MailGateway;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {
    /* TODO:
     * handleのテストでverifyは最小限。統合テストでDBの状態が正しいことを検証
     * MailTemplateのbuildに渡してる引数の中身の検証どうするか？
     */

    @Mock
    OrderMapper orderMapper;

    @Mock
    UserMapper userMapper;

    @Mock
    OrderGuard guard;

    @Mock
    MailGateway gateway;

    @InjectMocks
    OrderCommandService sut;

    Order before;
    Order after;
    OrderState next;

    final String ORDER_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeffffffff";
    final String USER_ID = "userId";
    final String key = "key";

    @BeforeEach
    void setup() {
        IdempotentExecutor executor = new IdempotentExecutor(mock(IdempotencyMapper.class));
        ReflectionTestUtils.setField(sut, "executor", executor);

        before = new Order() {
            {
                setOrderId(ORDER_ID);
                setOrderNumber(12);
                setUserId(USER_ID);
                setName("山田 太郎");
                setAddress("東京都 1丁目 2-3");
                setTotalQty(1);
                setOrderStatus(OrderStatus.OPEN);
                setShippingStatus(ShippingStatus.UNSHIPPED);
                setPaymentStatus(PaymentStatus.UNPAID);
            }
        };
        after = new Order() {
            {
                setOrderId(ORDER_ID);
                setOrderNumber(12);
                setUserId(USER_ID);
                setName("山田 太郎");
                setAddress("東京都 1丁目 2-3");
                setTotalQty(1);
                setOrderStatus(OrderStatus.OPEN);
                setShippingStatus(ShippingStatus.UNSHIPPED);
                setPaymentStatus(PaymentStatus.UNPAID);
            }
        };

        // guard.next は中身は問わない（cur の検証のみ行う）
        next = new OrderState(OrderStatus.OPEN, ShippingStatus.UNSHIPPED, PaymentStatus.UNPAID);

        // 1回目（apply前の参照）: before, 2回目（メール作成等で再取得）: after
        lenient().doReturn(before, after).when(orderMapper).selectOrderByPrimaryKey(anyString());
        lenient().doReturn(next).when(guard).next(any(), any(OrderEvent.class));
        lenient().doReturn(1).when(orderMapper).applyTransition(anyString(), any(), any());
        lenient().doReturn(Collections.<OrderItem> emptyList()).when(orderMapper).selectOrderItems(anyString());
        lenient().doReturn(new User() {
            {
                setUserId(USER_ID);
                setEmail("user@example.com");
            }
        }).when(userMapper).selectUserByPrimaryKey(anyString());
    }

    @Test
    void requestCancel() {
        sut.requestCancel(ORDER_ID);

        verify(orderMapper).applyTransition(
                anyString(),
                argThat(c -> c.getOrder() == OrderStatus.OPEN
                        && c.getShipping() == ShippingStatus.UNSHIPPED
                        && c.getPayment() == PaymentStatus.UNPAID),
                any());
        verify(gateway, never()).send(any());
    }

    @Test
    void approveCancel() {
        sut.approveCancel(ORDER_ID,key);

        verify(orderMapper).applyTransition(
                anyString(),
                argThat(c -> c.getOrder() == OrderStatus.OPEN
                        && c.getShipping() == ShippingStatus.UNSHIPPED
                        && c.getPayment() == PaymentStatus.UNPAID),
                any());
        verify(orderMapper).restoreInventory(anyString());
        verify(gateway).send(any());
    }

    @Nested
    class Ship {
        @Test
        void ship_fromOpen_sendsShippingMail() {
            // before は OPEN のまま
            sut.ship(ORDER_ID, key);

            verify(orderMapper).applyTransition(
                    anyString(),
                    argThat(c -> c.getOrder() == OrderStatus.OPEN
                            && c.getShipping() == ShippingStatus.UNSHIPPED
                            && c.getPayment() == PaymentStatus.UNPAID),
                    any());
            verify(orderMapper).updateShippedAt(anyString());
            verify(gateway).send(any());
        }

        @Test
        void ship_fromCancelRequested_sendsRejectMail() {
            before.setOrderStatus(OrderStatus.CANCEL_REQUESTED);

            sut.ship(ORDER_ID, key);

            verify(orderMapper).applyTransition(
                    anyString(),
                    argThat(c -> c.getOrder() == OrderStatus.CANCEL_REQUESTED
                            && c.getShipping() == ShippingStatus.UNSHIPPED
                            && c.getPayment() == PaymentStatus.UNPAID),
                    any());
            verify(orderMapper).updateShippedAt(anyString());
            verify(gateway).send(any());
        }

        @Test
        void ship_transitionNotAllowed_returns409WithReason() {
            doThrow(new IllegalStateException("error")).when(guard).next(any(), any());

            assertThatThrownBy(() -> sut.ship(ORDER_ID, key))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException e = (ResponseStatusException) ex;
                        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(e.getReason()).isEqualTo("ORDER_STATE_INVALID");
                    });
        }

        @Test
        void ship_updateConflict_returns409_ORDER_STATE_CONFLICT() {
            doReturn(0).when(orderMapper).applyTransition(anyString(), any(), any());

            assertThatThrownBy(() -> sut.ship(ORDER_ID, key))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException e = (ResponseStatusException) ex;
                        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(e.getReason()).isEqualTo("ORDER_STATE_CONFLICT");
                    });
        }
    }

    @Test
    void delivered_success() {
        // 配達完了は事前に出荷済み状態から想定
        before.setShippingStatus(ShippingStatus.SHIPPED);

        sut.markAsDelivered(ORDER_ID, key);

        verify(orderMapper).applyTransition(
                anyString(),
                argThat(c -> c.getOrder() == OrderStatus.OPEN
                        && c.getShipping() == ShippingStatus.SHIPPED
                        && c.getPayment() == PaymentStatus.UNPAID),
                any());
        verify(gateway, never()).send(any());
    }

}
