package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.OrderHistoryDto;
import com.example.dto.OrderItemDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.mapper.OrderHistoryMapper;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceTest {

    @Mock
    OrderHistoryMapper orderHistoryMapper;

    @InjectMocks
    OrderHistoryService orderHistoryService;

    @Nested
    class FindOrderHistories {
        String userId = "user";

        @Test
        void findOrderHistories_empty() {
            doReturn(Collections.EMPTY_LIST).when(orderHistoryMapper).selectHeadersByUser(userId);

            List<OrderHistoryDto> result = orderHistoryService.findOrderHistories(userId);

            assertThat(result).isEmpty();
        }

        @Test
        void findOrderHistories_multiple() {
            /* ---------- arrange ---------- */
            // ── ① ヘッダ 2 件（order-a は 1 商品、order-b は 2 商品） ──
            Order headerA = new Order();
            headerA.setOrderId("order-a");
            headerA.setOrderNumber(1);
            headerA.setUserId(userId);
            headerA.setName("山田 太郎");
            headerA.setPostalCode("1500000");
            headerA.setAddress("東京都渋谷区…");
            headerA.setTotalPrice(1_000);
            headerA.setCreatedAt(LocalDateTime.of(2025, 6, 29, 10, 0));

            Order headerB = new Order();
            headerB.setOrderId("order-b");
            headerB.setOrderNumber(3);
            headerB.setUserId(userId);
            headerB.setName("山田 花子");
            headerB.setPostalCode("1500001");
            headerB.setAddress("東京都新宿区…");
            headerB.setTotalPrice(5_000);
            headerB.setCreatedAt(LocalDateTime.of(2025, 4, 28, 11, 0));

            doReturn(List.of(headerA, headerB)).when(orderHistoryMapper).selectHeadersByUser(userId);

            // ── ② 明細（order-a 1件, order-b 2件） ──
            OrderItem a1 = new OrderItem();
            a1.setOrderId("order-a");
            a1.setProductId("p1");
            a1.setProductName("りんご");
            a1.setQty(1);
            a1.setPrice(1_000);
            a1.setSubtotal(1_000);

            OrderItem b1 = new OrderItem();
            b1.setOrderId("order-b");
            b1.setProductId("p2");
            b1.setProductName("みかん");
            b1.setQty(1);
            b1.setPrice(2_000);
            b1.setSubtotal(2_000);

            OrderItem b2 = new OrderItem();
            b2.setOrderId("order-b");
            b2.setProductId("p3");
            b2.setProductName("ぶどう");
            b2.setQty(1);
            b2.setPrice(3_000);
            b2.setSubtotal(3_000);

            doReturn(List.of(a1)).when(orderHistoryMapper).selectOrderItems("order-a");
            doReturn(List.of(b1, b2)).when(orderHistoryMapper).selectOrderItems("order-b");

            List<OrderHistoryDto> result = orderHistoryService.findOrderHistories(userId);

            assertThat(result).hasSize(2).extracting(
                    OrderHistoryDto::getOrderId,
                    OrderHistoryDto::getOrderNumber,
                    OrderHistoryDto::getOrderedAt,
                    OrderHistoryDto::getTotalPrice,
                    OrderHistoryDto::getName,
                    OrderHistoryDto::getPostalCode,
                    OrderHistoryDto::getAddress,
                    dto -> dto.getItems().size())
                    .containsExactly(
                            tuple(
                                    "order-a",
                                    "0001",
                                    LocalDate.of(2025, 6, 29),
                                    1000,
                                    "山田 太郎",
                                    "1500000",
                                    "東京都渋谷区…",
                                    1),
                            tuple(
                                    "order-b",
                                    "0003",
                                    LocalDate.of(2025, 4, 28),
                                    5000,
                                    "山田 花子",
                                    "1500001",
                                    "東京都新宿区…",
                                    2));

            OrderItemDto o1 = result.get(0).getItems().get(0);
            assertThat(o1.getProductId()).isEqualTo("p1");
            assertThat(o1.getProductName()).isEqualTo("りんご");
            assertThat(o1.getPrice()).isEqualTo(1000);
            assertThat(o1.getQty()).isEqualTo(1);
        }
    }

}
