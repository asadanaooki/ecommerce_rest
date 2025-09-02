package com.example.service.admin;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
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
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
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
    OrderMapper orderMapper;

    @Mock
    UserMapper userMapper;

    @Mock
    ProductMapper productMapper;

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
                    setShippingStatus(ShippingStatus.UNSHIPPED);
                }
            };
            doReturn(o).when(orderMapper).selectOrderByPrimaryKey(orderId);

            OrderItem i1 = item(orderId, "p1", 5, 1000);
            OrderItem i2 = item(orderId, "p2", 3, 2000);
            OrderItem i3 = item(orderId, "p3", 1, 500);
            OrderItem i4 = item(orderId, "p4", 4, 800);
            OrderItem i5 = item(orderId, "p5", 1, 100);

            // For the updated items after quantity reduction and deletion
            OrderItem i1Updated = item(orderId, "p1", 3, 1000);
            OrderItem i2Updated = item(orderId, "p2", 1, 2000);
            OrderItem i3Updated = item(orderId, "p3", 1, 500);

            // Mock the three calls to selectOrderItems in order
            when(orderMapper.selectOrderItems(orderId))
                    .thenReturn(List.of(i1, i2, i3, i4, i5))  // First call in prepareContext
                    .thenReturn(List.of(i1Updated, i2Updated, i3Updated))  // Second call for totals
                    .thenReturn(List.of(i1Updated, i2Updated, i3Updated)); // Third call for email

            User user = new User();
            user.setEmail("user@example.com");
            when(userMapper.selectUserByPrimaryKey(userId)).thenReturn(user);

            OrderEditRequest req = new OrderEditRequest() {
                {
                    setItems(Map.of("p1", 3, "p2", 1));  // Only items with quantity changes
                    setDeleted(List.of("p4", "p5"));
                }
            };

            adminOrderService.editOrder(orderId, req);

            verify(productMapper).increaseStock("p1", 2, null);
            verify(productMapper).increaseStock("p2", 2, null);
            verify(productMapper).increaseStock("p4", 4, null);
            verify(productMapper).increaseStock("p5", 1, null);
            verify(productMapper, times(4)).increaseStock(anyString(), anyInt(), any());

            verify(orderMapper).deleteOrderItem(orderId, "p4");
            verify(orderMapper).deleteOrderItem(orderId, "p5");
            verify(orderMapper, times(2)).deleteOrderItem(anyString(), anyString());

            ArgumentCaptor<OrderItem> cap = ArgumentCaptor.forClass(OrderItem.class);
            verify(orderMapper, times(2)).updateItemQty(cap.capture());
            List<OrderItem> updated = cap.getAllValues();
            assertThat(updated).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty)
                    .containsExactlyInAnyOrder(
                            tuple(
                                    orderId,
                                    "p1",
                                    3),
                            tuple(
                                    orderId,
                                    "p2",
                                    1));

            verify(orderMapper).updateTotals(eq(orderId), anyInt(), anyInt());

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
                    setShippingStatus(ShippingStatus.UNSHIPPED);
                }
            };
            doReturn(o).when(orderMapper).selectOrderByPrimaryKey(orderId);

            OrderItem i3 = item(orderId, "p3", 2, 500);

            // For the updated item after quantity reduction
            OrderItem i3Updated = item(orderId, "p3", 1, 500);

            // Mock the three calls to selectOrderItems in order
            when(orderMapper.selectOrderItems(orderId))
                    .thenReturn(List.of(i3))  // First call in prepareContext
                    .thenReturn(List.of(i3Updated))  // Second call for totals
                    .thenReturn(List.of(i3Updated)); // Third call for email

            User user = new User();
            user.setEmail("user@example.com");
            when(userMapper.selectUserByPrimaryKey(userId)).thenReturn(user);

            OrderEditRequest req = new OrderEditRequest() {
                {
                    setItems(Map.of("p3", 1));
                }
            };

            adminOrderService.editOrder(orderId, req);

            verify(productMapper).increaseStock("p3", 1, null);
            verify(orderMapper, never()).deleteOrderItem(anyString(), anyString());

            ArgumentCaptor<OrderItem> cap = ArgumentCaptor.forClass(OrderItem.class);
            verify(orderMapper).updateItemQty(cap.capture());
            OrderItem updated = cap.getValue();
            assertThat(updated).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty)
                    .containsExactly(
                            orderId,
                            "p3",
                            1);

            verify(orderMapper).updateTotals(eq(orderId), anyInt(), anyInt());

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
            doReturn(order).when(orderMapper).selectOrderByPrimaryKey(orderId);
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
            order.setShippingStatus(ShippingStatus.UNSHIPPED);

            OrderItem oldItem = new OrderItem();
            oldItem.setOrderId(orderId);
            oldItem.setProductId("p1");
            oldItem.setQty(5);
            oldItem.setUnitPriceIncl(1000);

            doReturn(order).when(orderMapper).selectOrderByPrimaryKey(orderId);
            doReturn(List.of(oldItem)).when(orderMapper).selectOrderItems(orderId);

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
            oi.setUnitPriceIncl(price);
            
            // Set subtotal using reflection since setter is private
            try {
                Field subtotalField = OrderItem.class.getDeclaredField("subtotalIncl");
                subtotalField.setAccessible(true);
                subtotalField.set(oi, qty * price);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set subtotalIncl", e);
            }
            
            return oi;
        }
    }

}
