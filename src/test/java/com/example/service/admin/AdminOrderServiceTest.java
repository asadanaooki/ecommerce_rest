package com.example.service.admin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;
import com.example.mapper.UserMapper;
import com.example.mapper.admin.AdminOrderMapper;
import com.example.request.admin.OrderEditRequest;
import com.example.support.MailGateway;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @InjectMocks
    AdminOrderService adminOrderService;

    @Mock
    AdminOrderMapper adminOrderMapper;

    @Mock
    UserMapper userMapper;

    @Mock
    MailGateway gateway;


    @Nested
    class editOrder {
        String orderId = "id";

        String userId = "user";

        @Test
        void editOrder_reduceAndDelete() {
            Order o = new Order() {
                {
                    setOrderId(orderId);
                    setOrderNumber(20);
                    setUserId(userId);
                    setPaymentStatus(PaymentStatus.UNPAID);
                    setShippingStatus(ShippingStatus.NOT_SHIPPED);
                }
            };
            doReturn(o).when(adminOrderMapper).selectOrderForUpdate(orderId);

            OrderItem i1 = item(orderId, "p1", 5, 1000);
            OrderItem i2 = item(orderId, "p2", 3, 2000);
            OrderItem i3 = item(orderId, "p3", 1, 500);
            OrderItem i4 = item(orderId, "p4", 4, 800);
            OrderItem i5 = item(orderId, "p5", 1, 100);
            doReturn(List.of(i1, i2, i3, i4, i5)).when(adminOrderMapper).selectOrderItemsForUpdate(orderId);

            User user = new User();
            user.setUserId(userId);
            user.setEmail("user@example.com");
            when(userMapper.selectUserByPrimaryKey(userId)).thenReturn(user);

            OrderEditRequest req = new OrderEditRequest() {
                {
                    setItems(Map.of("p1", 3, "p2", 1, "p3", 1));
                    setDeleted(List.of("p4", "p5"));
                }
            };

            adminOrderService.editOrder(orderId, req);

            verify(adminOrderMapper).addStock("p1", 2);
            verify(adminOrderMapper).addStock("p2", 2);
            verify(adminOrderMapper).addStock("p4", 4);
            verify(adminOrderMapper).addStock("p5", 1);
            verify(adminOrderMapper, times(4)).addStock(anyString(), anyInt());

            verify(adminOrderMapper).deleteOrderItem(orderId, "p4");
            verify(adminOrderMapper).deleteOrderItem(orderId, "p5");
            verify(adminOrderMapper, times(2)).deleteOrderItem(anyString(), anyString());

            ArgumentCaptor<OrderItem> cap = ArgumentCaptor.forClass(OrderItem.class);
            verify(adminOrderMapper, times(2)).updateItemQty(cap.capture());
            List<OrderItem> updated = cap.getAllValues();
            assertThat(updated).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty,
                    OrderItem::getSubtotal)
                    .containsExactlyInAnyOrder(
                            tuple(
                                    orderId,
                                    "p1",
                                    3,
                                    3000),
                            tuple(
                                    orderId,
                                    "p2",
                                    1,
                                    2000));

            verify(adminOrderMapper).updateTotals(orderId);

            verify(gateway).send(any());
        }

        @Test
        void editOrder_updateOnly() {
            Order o = new Order() {
                {
                    setOrderId(orderId);
                    setOrderNumber(20);
                    setUserId(userId);
                    setPaymentStatus(PaymentStatus.UNPAID);
                    setShippingStatus(ShippingStatus.NOT_SHIPPED);
                }
            };
            doReturn(o).when(adminOrderMapper).selectOrderForUpdate(orderId);

            OrderItem i3 = item(orderId, "p3", 2, 500);
            doReturn(List.of(i3)).when(adminOrderMapper).selectOrderItemsForUpdate(orderId);

            User user = new User();
            user.setUserId(userId);
            user.setEmail("user@example.com");
            when(userMapper.selectUserByPrimaryKey(userId)).thenReturn(user);

            OrderEditRequest req = new OrderEditRequest() {
                {
                    setItems(Map.of("p3", 1));
                }
            };

            adminOrderService.editOrder(orderId, req);

            verify(adminOrderMapper).addStock(anyString(), anyInt());
            verify(adminOrderMapper, never()).deleteOrderItem(anyString(), anyString());

            ArgumentCaptor<OrderItem> cap = ArgumentCaptor.forClass(OrderItem.class);
            verify(adminOrderMapper).updateItemQty(cap.capture());
            OrderItem updated = cap.getValue();
            assertThat(updated).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty,
                    OrderItem::getSubtotal)
                    .containsExactly(
                            orderId,
                            "p3",
                            1,
                            500);

            verify(adminOrderMapper).updateTotals(orderId);

            verify(gateway).send(any());
        }

        @Test
        void editOrder_statusViolation() {

            Order order = new Order() {
                {
                    setOrderId(orderId);
                    setPaymentStatus(PaymentStatus.UNPAID);
                    setShippingStatus(ShippingStatus.SHIPPED);
                }
            };
            doReturn(order).when(adminOrderMapper).selectOrderForUpdate(orderId);
            OrderEditRequest req = new OrderEditRequest();

            assertThatThrownBy(() -> adminOrderService.editOrder(orderId, req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            e -> {
                                assertThat(((ResponseStatusException) e).getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT);
                                assertThat(((ResponseStatusException) e).getReason())
                                        .isEqualTo("STATUS_NOT_EDITABLE");
                            });
        }

        @Test
        void editOrder_qtyIncrease() {
            Order order = new Order();
            order.setOrderId(orderId);
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setShippingStatus(ShippingStatus.NOT_SHIPPED);

            OrderItem oldItem = new OrderItem();
            oldItem.setOrderId(orderId);
            oldItem.setProductId("p1");
            oldItem.setQty(5);
            oldItem.setPrice(1000);
            oldItem.setSubtotal(5000);

            doReturn(order).when(adminOrderMapper).selectOrderForUpdate(orderId);
            doReturn(List.of(oldItem)).when(adminOrderMapper).selectOrderItemsForUpdate(orderId);

            OrderEditRequest req = new OrderEditRequest() {
                {
                    setItems(Map.of("p1", 6));
                }
            };

            assertThatThrownBy(() -> adminOrderService.editOrder(orderId, req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            e -> {
                                assertThat(((ResponseStatusException) e).getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT);
                                assertThat(((ResponseStatusException) e).getReason())
                                        .isEqualTo("QUANTITY_INCREASE_NOT_ALLOWED");
                            });
        }

        private OrderItem item(String oid, String pid, int qty, int price) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(oid);
            oi.setProductId(pid);
            oi.setProductName(pid + "_name");
            oi.setQty(qty);
            oi.setPrice(price);
            oi.setSubtotal(qty * price);
            return oi;
        }
    }

}
