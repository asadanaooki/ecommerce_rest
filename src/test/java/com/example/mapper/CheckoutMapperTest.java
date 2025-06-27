package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.example.dto.CartItemDto;
import com.example.entity.CartItem;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.testUtil.TestDataFactory;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
class CheckoutMapperTest {

    @Autowired
    CheckoutMapper checkoutMapper;
    
    @Autowired
    TestDataFactory factory;
    
    String userId = "550e8400-e29b-41d4-a716-446655440000";
    
    String cartId = "bbbbeeee-cccc-dddd-aaaa-111122223333";
    
    @Test
    void selectCheckoutItems() {
        LocalDateTime time1 = LocalDateTime.of(2025, 6, 22, 10, 40, 3);
        LocalDateTime time2 = LocalDateTime.of(2025, 6, 21, 15, 42, 3);
        
        factory.deleteCart(cartId);
        factory.createCart(cartId, userId);
        factory.createCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                setQty(1);
                setPrice(750);
                setCreatedAt(time1);
                setUpdatedAt(time1);
            }
        });
        factory.createCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
                setQty(1);
                setPrice(3300);
                setCreatedAt(time1);
                setUpdatedAt(time1);
            }
        });
        factory.createCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId("4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07");
                setQty(4);
                setPrice(1800);
                setCreatedAt(time2);
                setUpdatedAt(time2);
            }
        });
        
        List<CartItemDto> items = checkoutMapper.selectCheckoutItems("bbbbeeee-cccc-dddd-aaaa-111122223333");
        
        assertThat(items).hasSize(3);
        assertThat(items.get(0)).extracting(
                CartItemDto::getProductId,
                CartItemDto::getProductName,
                CartItemDto::getQty,
                CartItemDto::getPriceEx,
                CartItemDto::getPriceAtCartAddition,
                CartItemDto::getPriceInc,
                CartItemDto::getSubtotal,
                CartItemDto::getStatus,
                CartItemDto::getStock,
                CartItemDto::getReason
                )
        .containsExactly(
                "f9c9cfb2-0893-4f1c-b508-f9e909ba5274",
                "Item18",
                1,
                3200,
                3300,
                null,
                0,
                "1",
                15,
                null
                );
    }
    
    @Test
    void insertOrderHeader() {
        Order order = new Order();
        order.setOrderId(cartId);
        order.setUserId(userId);
        order.setName("山田 太郎");
        order.setPostalCode("1500041");
        order.setAddress("東京都渋谷区神南1-1-1");
        order.setTotalQty(3);
        order.setTotalPrice(9600);
        
        int row = checkoutMapper.insertOrderHeader(order);
        
        assertThat(row).isOne();
        Order saved = checkoutMapper.selectOrderByPrimaryKey(cartId);
        
        assertThat(saved).extracting(
                Order::getOrderId,
                Order::getUserId,
                Order::getName,
                Order::getPostalCode,
                Order::getAddress,
                Order::getTotalQty,
                Order::getTotalPrice
                )
        .containsExactly(
                cartId,
                userId,
                "山田 太郎",
                "1500041",
                "東京都渋谷区神南1-1-1",
                3,
                9600
                );
    }
    
    @Test
    void insertOrderItems() {
        // ---------- arrange ----------
        // 1) 注文ヘッダを先に作成
        String orderId = cartId;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setName("山田 太郎");
        order.setPostalCode("1500041");
        order.setAddress("東京都渋谷区神南1-1-1");
        order.setTotalQty(3);
        order.setTotalPrice(9600);
        checkoutMapper.insertOrderHeader(order);

        // 2) 明細リスト作成
        OrderItem it1 = new OrderItem();
        it1.setOrderId(orderId);
        it1.setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
        it1.setQty(1);
        it1.setPrice(750);
        it1.setSubtotal(750);

        OrderItem it2 = new OrderItem();
        it2.setOrderId(orderId);
        it2.setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
        it2.setQty(2);
        it2.setPrice(3200);
        it2.setSubtotal(6400);

        List<OrderItem> items = List.of(it1, it2);
        
        int rows = checkoutMapper.insertOrderItems(items);
        assertThat(rows).isEqualTo(2);
        
        List<OrderItem> saved = checkoutMapper.selectOrderItems(orderId);
        assertThat(saved).hasSize(2);
        OrderItem order1 = saved.get(0);
        assertThat(order1.getOrderId()).isEqualTo(orderId);
        assertThat(order1.getProductId()).isEqualTo("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
        assertThat(order1.getQty()).isEqualTo(1);
        assertThat(order1.getPrice()).isEqualTo(750);
        assertThat(order1.getSubtotal()).isEqualTo(750);
        assertThat(order1.getCreatedAt()).isNotNull();
        assertThat(order1.getUpdatedAt()).isNotNull();
        
        assertThat(saved.get(1).getProductId()).isEqualTo("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
    }

}
