package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.example.dto.CheckoutItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.enums.PaymentStatus;
import com.example.enums.SaleStatus;
import com.example.enums.ShippingStatus;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
class CheckoutMapperTest {

    @Autowired
    CheckoutMapper checkoutMapper;

    @Autowired
    OrderHistoryMapper orderHistoryMapper;

    @Autowired
    TestDataFactory factory;

    String userId = "550e8400-e29b-41d4-a716-446655440000";

    String cartId = "bbbbeeee-cccc-dddd-aaaa-111122223333";

    @Test
    void selectCheckoutItems() {
        LocalDateTime time1 = LocalDateTime.of(2025, 6, 22, 10, 40, 3);
        LocalDateTime time2 = LocalDateTime.of(2025, 6, 21, 15, 42, 3);

        factory.deleteCart(cartId);

        factory.createCart(new Cart() {
            {
                setCartId(cartId);
                setUserId(userId);
                setTtlDays(60);
            }
        });
        factory.createCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                setQty(1);
                setUnitPriceExcl(750);
                setCreatedAt(time1);
                setUpdatedAt(time1);
            }
        });
        factory.createCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
                setQty(1);
                setUnitPriceExcl(3300);
                setCreatedAt(time1);
                setUpdatedAt(time1);
            }
        });
        factory.createCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId("4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07");
                setQty(4);
                setUnitPriceExcl(1800);
                setCreatedAt(time2);
                setUpdatedAt(time2);
            }
        });

        List<CheckoutItemDto> items = checkoutMapper.selectCheckoutItems("bbbbeeee-cccc-dddd-aaaa-111122223333");

        assertThat(items).hasSize(3);
        assertThat(items.get(0)).extracting(
                CheckoutItemDto::getProductId,
                CheckoutItemDto::getProductName,
                CheckoutItemDto::getQty,
                CheckoutItemDto::getCurrentUnitPriceExcl,
                CheckoutItemDto::getUnitPriceExclAtAddToCart,
                CheckoutItemDto::getUnitPriceIncl,
                CheckoutItemDto::getSubtotalIncl,
                CheckoutItemDto::getStatus,
                CheckoutItemDto::getStock,
                CheckoutItemDto::getReason)
                .containsExactly(
                        "f9c9cfb2-0893-4f1c-b508-f9e909ba5274",
                        "Item18",
                        1,
                        3200,
                        3300,
                        null,
                        null,
                        SaleStatus.PUBLISHED,
                        15,
                        null);
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
        order.setTotalPriceIncl(9600);

        checkoutMapper.insertOrderHeader(order);

        assertThat(order.getOrderNumber()).isEqualTo(3);
        Order saved = checkoutMapper.selectOrderByPrimaryKey(cartId);

        assertThat(saved).extracting(
                Order::getOrderId,
                Order::getUserId,
                Order::getName,
                Order::getPostalCode,
                Order::getAddress,
                Order::getTotalQty,
                Order::getTotalPriceIncl,
                Order::getShippingStatus,
                Order::getPaymentStatus)
                .containsExactly(
                        cartId,
                        userId,
                        "山田 太郎",
                        "1500041",
                        "東京都渋谷区神南1-1-1",
                        3,
                        9600,
                        ShippingStatus.NOT_SHIPPED,
                        PaymentStatus.UNPAID);
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
        order.setTotalPriceIncl(9600);
        checkoutMapper.insertOrderHeader(order);

        // 2) 明細リスト作成
        OrderItem it1 = new OrderItem();
        it1.setOrderId(orderId);
        it1.setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
        it1.setProductName("testA");
        it1.setQty(1);
        it1.setUnitPriceIncl(750);
        it1.setSubtotalIncl(750);

        OrderItem it2 = new OrderItem();
        it2.setOrderId(orderId);
        it2.setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
        it2.setProductName("testB");
        it2.setQty(2);
        it2.setUnitPriceIncl(3200);
        it2.setSubtotalIncl(6400);

        List<OrderItem> items = List.of(it1, it2);

        int rows = checkoutMapper.insertOrderItems(items);
        assertThat(rows).isEqualTo(2);

        List<OrderItem> saved = orderHistoryMapper.selectOrderItems(orderId);
        assertThat(saved).hasSize(2);
        OrderItem order1 = saved.get(0);
        assertThat(order1.getOrderId()).isEqualTo(orderId);
        assertThat(order1.getProductId()).isEqualTo("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
        assertThat(order1.getProductName()).isEqualTo("testA");
        assertThat(order1.getQty()).isEqualTo(1);
        assertThat(order1.getUnitPriceIncl()).isEqualTo(750);
        assertThat(order1.getSubtotalIncl()).isEqualTo(750);
        assertThat(order1.getCreatedAt()).isNotNull();
        assertThat(order1.getUpdatedAt()).isNotNull();

        assertThat(saved.get(1).getProductId()).isEqualTo("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
    }

    //    @Test
    //    void selectHeadersByUser() throws InterruptedException {
    //        String orderId2 = "22222222-2222-2222-2222-222222222222";
    //        Order o2 = new Order();
    //        o2.setOrderId(orderId2);
    //        o2.setUserId(userId);
    //        o2.setName("山田 次郎");
    //        o2.setPostalCode("1600000");
    //        o2.setAddress("東京都新宿区西新宿2-2-2");
    //        o2.setTotalQty(1);
    //        o2.setTotalPriceIncl(3200);
    //        checkoutMapper.insertOrderHeader(o2);
    //        
    //        Thread.sleep(2000);           // 1 秒待機（テスト用なので簡易に）
    //        
    //        String orderId1 = "11111111-1111-1111-1111-111111111111";
    //        Order o1 = new Order();
    //        o1.setOrderId(orderId1);
    //        o1.setUserId(userId);
    //        o1.setName("山田 太郎");
    //        o1.setPostalCode("1500041");
    //        o1.setAddress("東京都渋谷区神南1-1-1");
    //        o1.setTotalQty(3);
    //        o1.setTotalPriceIncl(9600);
    //        checkoutMapper.insertOrderHeader(o1);
    //        
    //        List<OrderHeaderDto> headers = checkoutMapper.selectHeadersByUser(userId);
    //        
    //        assertThat(headers).hasSize(2);
    //        assertThat(headers.get(1).getOrderId()).isEqualTo(orderId2);
    //        
    //        assertThat(headers.get(0)).extracting(
    //                OrderHeaderDto::getOrderId,
    //                OrderHeaderDto::getTotalPrice,
    //                OrderHeaderDto::getName,
    //                OrderHeaderDto::getPostalCode,
    //                OrderHeaderDto::getAddress
    //                )
    //        .containsExactly(
    //                orderId1,
    //                9600,
    //                "山田 太郎",
    //                "1500041",
    //                "東京都渋谷区神南1-1-1"
    //                );
    //        assertThat(headers.get(0).getCreatedAt()).isNotNull();
    //    }
    //    
    //    @Test
    //    void selectItemsByOrderIds() {
    //     // ---------- arrange ----------
    //        // 注文ヘッダ 1（明細は 2 商品）
    //        String orderId1 = "33333333-3333-3333-3333-333333333333";
    //        Order h1 = new Order();
    //        h1.setOrderId(orderId1);
    //        h1.setUserId(userId);
    //        h1.setName("山田 花子");
    //        h1.setPostalCode("1000001");
    //        h1.setAddress("東京都千代田区千代田1-1-1");
    //        h1.setTotalQty(3);
    //        h1.setTotalPriceIncl(7150);
    //        checkoutMapper.insertOrderHeader(h1);
    //
    //        OrderItem h1i1 = new OrderItem();
    //        h1i1.setOrderId(orderId1);
    //        h1i1.setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
    //        h1i1.setProductName("testC");
    //        h1i1.setQty(1);
    //        h1i1.setPrice(750);
    //        h1i1.setSubtotalIncl(750);
    //
    //        OrderItem h1i2 = new OrderItem();
    //        h1i2.setOrderId(orderId1);
    //        h1i2.setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
    //        h1i2.setProductName("testD");
    //        h1i2.setQty(2);
    //        h1i2.setPrice(3200);
    //        h1i2.setSubtotalIncl(6400);
    //
    //        checkoutMapper.insertOrderItems(List.of(h1i1, h1i2));
    //
    //        // 注文ヘッダ 2（明細は 1 商品）
    //        String orderId2 = "44444444-4444-4444-4444-444444444444";
    //        Order h2 = new Order();
    //        h2.setOrderId(orderId2);
    //        h2.setUserId(userId);
    //        h2.setName("山田 三郎");
    //        h2.setPostalCode("1000002");
    //        h2.setAddress("東京都千代田区丸の内2-2-2");
    //        h2.setTotalQty(1);
    //        h2.setTotalPriceIncl(1800);
    //        checkoutMapper.insertOrderHeader(h2);
    //
    //        OrderItem h2i1 = new OrderItem();
    //        h2i1.setOrderId(orderId2);
    //        h2i1.setProductId("09d5a43a-d24c-41c7-af2b-9fb7b0c9e049");
    //        h2i1.setProductName("testE");
    //        h2i1.setQty(1);
    //        h2i1.setPrice(1800);
    //        h2i1.setSubtotalIncl(1800);
    //
    //        checkoutMapper.insertOrderItems(List.of(h2i1));
    //        List<String> orderIds = List.of(orderId1, orderId2);
    //        
    //        List<OrderItemDto> items = checkoutMapper.selectItemsByOrderIds(orderIds);
    //        
    //        
    //    }

}
