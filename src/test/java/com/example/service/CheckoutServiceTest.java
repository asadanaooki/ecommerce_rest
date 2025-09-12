package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.dto.CartItemDto;
import com.example.dto.CheckoutConfirmDto;
import com.example.dto.CheckoutItemDto;
import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.SaleStatus;
import com.example.error.BusinessException;
import com.example.feature.cart.CartGuard;
import com.example.mapper.CartMapper;
import com.example.mapper.IdempotencyMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.support.IdempotentExecutor;
import com.example.support.MailGateway;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    CartMapper cartMapper;

    @Mock
    UserMapper userMapper;

    @Mock
    ProductMapper productMapper;

    @Mock
    OrderMapper orderMapper;

    @Mock
    IdempotencyMapper idempotencyMapper;

    @Mock
    CartGuard cartGuard;

    @InjectMocks
    CheckoutService checkoutService;

    @Mock
    MailGateway gateway;
    
    
    @BeforeEach
    void init() {
        IdempotentExecutor executor = spy(new IdempotentExecutor(idempotencyMapper));
        ReflectionTestUtils.setField(checkoutService, "executor", executor);
    }

    @Nested
    class LoadCheckout {
        String userId = "user";
        User user;
        Cart cart;

        @BeforeEach
        void setup() {
            User u = new User();
            u.setUserId(userId);
            u.setLastName("山田");
            u.setFirstName("太郎");
            u.setAddressPrefCity("東京都千代田区");
            u.setAddressArea("丸の内");
            u.setAddressBlock("1-1-1");
            u.setAddressBuilding(null); // 建物名は無し
            u.setPostalCode("1000001");
            user = u;
            cart = new Cart() {
                {
                    //  setVersion(1);
                }
            };

            lenient().doReturn(cart).when(cartMapper).selectCartByUser(userId);
            lenient().doReturn(user).when(userMapper).selectUserByPrimaryKey(userId);
        }

        //        @Test
        //        void loadCheckout_autoDeletionTarget() {
        //            List<CartItemDto> autoDeletionTargets = List.of(
        //                    // ① 販売停止品
        //                    new CartItemDto() {
        //                        {
        //                            setProductId("A-001");
        //                            setProductName("販売停止品");
        //                            setQty(1);
        //                            setCurrentUnitPriceExcl(100);
        //                            setUnitPriceExclAtAddToCart(100);
        //                        }
        //                    },
        //                    // ② 在庫切れ品
        //                    new CartItemDto() {
        //                        {
        //                            setProductId("B-002");
        //                            setProductName("在庫切れ品");
        //                            setStatus("1"); // 販売中
        //                            setAvailable(0); // stock <= 0 → OUT_OF_STOCK
        //                            setQty(2);
        //                            setCurrentUnitPriceExcl(200);
        //                            setUnitPriceExclAtAddToCart(200);
        //                        }
        //                    });
        //            doReturn(autoDeletionTargets).when(cartMapper).selectCartItems("cartId");
        //
        //            CheckoutDto dto = checkoutService.loadCheckout(userId);
        //
        //            assertThat(dto.getItems()).isEmpty();
        //            assertThat(dto.getRemoved())
        //                    .extracting(RemovedItemDto::getProductName, RemovedItemDto::getReason)
        //                    .containsExactly(tuple("販売停止品", RemovedItemDto.Reason.DISCONTINUED),
        //                            tuple("在庫切れ品", RemovedItemDto.Reason.OUT_OF_STOCK));
        //            verify(cartMapper).deleteRemovedItems("cartId", List.of("A-001", "B-002"));
        //
        //        }

        @Test
        void loadCheckout_cartNotFound() {
            cart.setCartId("cartId");
            doThrow(new BusinessException(HttpStatus.NOT_FOUND, "CART_NOT_FOUND"))
            .when(cartGuard).requireByUserId(userId);

            assertThatThrownBy(() -> checkoutService.loadCheckout(userId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
        }

        @Test
        void loadCheckout_cartExists() {
            CartItemDto item1 = new CartItemDto();
            item1.setProductId("N-005");
            item1.setProductName("通常品");
            item1.setQty(2);
            item1.setUnitPriceIncl(220);
            item1.setSubtotalIncl(440);

            CartItemDto item2 = new CartItemDto();
            item2.setProductId("N-006");
            item2.setProductName("通常品2");
            item2.setQty(1);
            item2.setUnitPriceIncl(110);
            item2.setSubtotalIncl(110);

            List<CartItemDto> items = List.of(item1, item2);
            cart.setCartId("cartId");
            doReturn(cart).when(cartGuard).requireByUserId(userId);
            doReturn(items).when(cartMapper).selectCartItems("cartId");

            CheckoutConfirmDto dto = checkoutService.loadCheckout(userId);

            assertThat(dto.getUsername()).isEqualTo("山田 太郎");
            assertThat(dto.getPostalCode()).isEqualTo("1000001");
            assertThat(dto.getAddress()).isNotBlank();

            assertThat(dto.getCart().getItems()).hasSize(2).extracting(
                    CartItemDto::getProductId,
                    CartItemDto::getProductName,
                    CartItemDto::getQty,
                    CartItemDto::getUnitPriceIncl,
                    CartItemDto::getSubtotalIncl)
                    .containsExactly(tuple("N-005", "通常品", 2, 220, 440),
                            tuple("N-006", "通常品2", 1, 110, 110));

            assertThat(dto.getCart().getItemsSubtotalIncl()).isEqualTo(550);
            assertThat(dto.getCart().getShippingFeeIncl()).isEqualTo(0);
            assertThat(dto.getCart().getCodFeeIncl()).isEqualTo(330);
            assertThat(dto.getCart().getGrandTotalIncl()).isEqualTo(880);
            assertThat(dto.getCart().getTotalQty()).isEqualTo(3);
        }

        //        @Test
        //        void loadCheckout_mixedItems() {
        //            List<CartItemDto> mixed = List.of(
        //                    // ① 販売停止
        //                    new CartItemDto() {{
        //                        setProductId("D-001"); setProductName("販売停止品");
        //                        setStatus("0");            // STATUS_DISCONTINUED
        //                        setAvailable(10); setQty(1);
        //                        setPriceEx(100); setPriceAtCartAddition(100);
        //                    }},
        //                    // ③ 在庫不足
        //                    new CartItemDto() {{
        //                        setProductId("L-003"); setProductName("在庫不足品");
        //                        setStatus("1");
        //                        setAvailable(1);  setQty(3);   // stock < qty
        //                        setPriceEx(120); setPriceAtCartAddition(120);
        //                    }},
        //                    // ④ 価格改定
        //                    new CartItemDto() {{
        //                        setProductId("P-004"); setProductName("価格改定品");
        //                        setStatus("1");
        //                        setAvailable(5);  setQty(1);
        //                        setPriceEx(150); setPriceAtCartAddition(100); // 価格改定
        //                    }},
        //                    // ⑤ 正常品
        //                    new CartItemDto() {{
        //                        setProductId("N-005"); setProductName("通常品");
        //                        setStatus("1");
        //                        setAvailable(20); setQty(2);
        //                        setPriceEx(200); setPriceAtCartAddition(200);
        //                    }}
        //                );
        //            doReturn(mixed).when(cartMapper).selectCartItems("cartId");
        //            
        //            CheckoutDto dto = checkoutService.loadCheckout(userId);
        //
        //            assertThat(dto.getUsername()).isEqualTo("山田 太郎");
        //            assertThat(dto.getPostalCode()).isEqualTo("1000001");
        //            assertThat(dto.getAddress()).isNotBlank();
        //            assertThat(dto.getItems()).hasSize(3);
        //            assertThat(dto.getRemoved()).hasSize(1);
        //            assertThat(dto.getTotalQty()).isEqualTo(6);
        //            assertThat(dto.getGrandTotalIncl()).isEqualTo(1001);
        //            
        //            assertThat(dto.getItems().get(0).getLowStock()).isTrue();
        //            assertThat(dto.getItems().get(0).getStockJson()).isOne();
        //            assertThat(dto.getItems().get(1).isPriceChanged()).isTrue();
        //            
        //            assertThat(dto.getItems().get(2)).extracting(
        //                    CartItemDto::getProductId,
        //                    CartItemDto::getProductName,
        //                    CartItemDto::getQty,
        //                    CartItemDto::getPriceInc,
        //                    CartItemDto::getSubtotal,
        //                    CartItemDto::getStatus,
        //                    CartItemDto::getLowStock,
        //                    CartItemDto::getStockJson,
        //                    CartItemDto::isPriceChanged
        //                    )
        //            .containsExactly(
        //                    "N-005",
        //                    "通常品",
        //                    2,
        //                    220,
        //                    440,
        //                    "1",
        //                    false,
        //                    null,
        //                    false
        //                    );
        //            
        //            verify(cartMapper).deleteRemovedItems(anyString(), anyList());
        //        }
        //        
        //        @Test
        //        void loadCheckout_noRemovedItems() {
        //            List<CartItemDto> valid = List.of(
        //                    new CartItemDto() {{
        //                        setProductId("N-010"); setProductName("通常品");
        //                        setStatus("1"); setAvailable(10); setQty(1);
        //                        setPriceEx(100); setPriceAtCartAddition(100);
        //                    }}
        //                );
        //            doReturn(valid).when(cartMapper).selectCartItems(anyString());
        //            
        //            CheckoutDto dto = checkoutService.loadCheckout(userId);
        //            
        //            assertThat(dto.getItems()).hasSize(1);
        //            assertThat(dto.getRemoved()).isEmpty();
        //            verify(cartMapper, never()).deleteRemovedItems(anyString(), anyList());
        //        }
    }

    @Nested
    class Checkout {
        String userId = "user";
        String cartId = "cartId";
        String idempotency = "key";

        //        @Test
        //        void checkout_cartVersionMismatch() {
        //            int passedVer = 1; // テスト入力
        //            int currentVer = 2; // DB 上のバージョン（≠ passedVer）
        //
        //            Cart cart = new Cart() {
        //                {
        //                    setCartId(cartId);
        //                    setUserId(userId);
        //                    setVersion(currentVer);
        //                }
        //            };
        //            doReturn(cart).when(cartMapper).selectCartByUser(userId);
        //
        //            assertThatThrownBy(() -> checkoutService.checkout(userId, passedVer))
        //                    .isInstanceOf(BusinessException.class)
        //                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT);
        //        }
        @Test
        void checkout_calledTwice() throws MessagingException {
            doThrow(new DuplicateKeyException(""))
            .when(idempotencyMapper).insert(anyString());

            checkoutService.checkout(userId, idempotency);

            verify(cartMapper, never()).selectCartByUser(anyString());
        }

        @Test
        void checkout_diffItemsAllKinds() {
            // diff が発生する 4 件分の CheckoutItemDto
            CheckoutItemDto discontinued = new CheckoutItemDto();
            discontinued.setStatus(SaleStatus.UNPUBLISHED);
            discontinued.setReason(CheckoutItemDto.DiffReason.DISCONTINUED);

            CheckoutItemDto outOfStock = new CheckoutItemDto();
            outOfStock.setAvailable(0);
            outOfStock.setReason(CheckoutItemDto.DiffReason.OUT_OF_STOCK);

            CheckoutItemDto lowStock = new CheckoutItemDto();
            lowStock.setAvailable(1);
            lowStock.setQty(3);
            lowStock.setReason(CheckoutItemDto.DiffReason.LOW_STOCK);

            CheckoutItemDto priceChanged = new CheckoutItemDto();
            priceChanged.setCurrentUnitPriceExcl(150);
            priceChanged.setUnitPriceExclAtAddToCart(100);
            priceChanged.setAvailable(10);
            priceChanged.setQty(1);
            priceChanged.setStatus(SaleStatus.PUBLISHED);

            List<CheckoutItemDto> diffItems = List.of(
                    discontinued,
                    outOfStock,
                    lowStock,
                    priceChanged);
            doReturn(new Cart() {
                {
                    setCartId(cartId);
                    //    setVersion(2);
                }
            }).when(cartMapper).selectCartByUser(userId);
            doReturn(diffItems).when(orderMapper).selectCheckoutItems(cartId);

            assertThatThrownBy(() -> checkoutService.checkout(userId, idempotency))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(t -> {
                        BusinessException e = (BusinessException) t;
                        assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(e.getErrorCode()).isEqualTo("diff");
                        assertThat((List<CheckoutItemDto>) e.getData()).extracting(CheckoutItemDto::getReason)
                                .containsExactly(
                                        CheckoutItemDto.DiffReason.DISCONTINUED,
                                        CheckoutItemDto.DiffReason.OUT_OF_STOCK,
                                        CheckoutItemDto.DiffReason.LOW_STOCK,
                                        CheckoutItemDto.DiffReason.PRICE_CHANGED);
                    });

        }

        @Test
        void checkout_success() throws MessagingException {
            Cart cart = new Cart() {
                {
                    setCartId(cartId);
                    //    setVersion(2);
                }
            };
            CheckoutItemDto item1 = new CheckoutItemDto();
            item1.setProductId("P1");
            item1.setProductName("Product 1");
            item1.setQty(2);
            item1.setUnitPriceIncl(100);
            item1.setStatus(SaleStatus.PUBLISHED);
            item1.setAvailable(10);
            item1.setCurrentUnitPriceExcl(100);
            item1.setUnitPriceExclAtAddToCart(100);
            item1.setSubtotalIncl(200);

            CheckoutItemDto item2 = new CheckoutItemDto();
            item2.setProductId("P2");
            item2.setProductName("Product 2");
            item2.setQty(20);
            item2.setUnitPriceIncl(150);
            item2.setStatus(SaleStatus.PUBLISHED);
            item2.setAvailable(100);
            item2.setCurrentUnitPriceExcl(150);
            item2.setSubtotalIncl(3000);
            item2.setUnitPriceExclAtAddToCart(150);

            List<CheckoutItemDto> items = List.of(item1, item2);
            User user = new User() {
                {
                    setUserId(userId);
                    setLastName("山田");
                    setFirstName("太郎");
                    setAddressPrefCity("東京都千代田区");
                    setAddressArea("丸の内");
                    setAddressBlock("1-1-1");
                    setPostalCode("1000001");
                    setEmail("yamada@example.com");
                }
            };
            doReturn(cart).when(cartMapper).selectCartByUser(userId);
            doReturn(items).when(orderMapper).selectCheckoutItems(cartId);
            doReturn(user).when(userMapper).selectUserByPrimaryKey(userId);

            checkoutService.checkout(userId, idempotency);

            ArgumentCaptor<Order> headerCap = ArgumentCaptor.forClass(Order.class);
            verify(orderMapper).insertOrderHeader(headerCap.capture());
            Order header = headerCap.getValue();
            assertThat(header).extracting(
                    Order::getOrderId,
                    Order::getUserId,
                    Order::getName,
                    Order::getPostalCode,
                    Order::getAddress,
                    Order::getTotalQty,
                    Order::getItemsSubtotalIncl,
                    Order::getShippingFeeIncl,
                    Order::getCodFeeIncl).containsExactly(
                            cartId,
                            userId,
                            "山田 太郎",
                            "1000001",
                            "東京都千代田区丸の内1-1-1",
                            22,
                            3200,
                            500,
                            330);

            ArgumentCaptor<List<OrderItem>> itemsCap = ArgumentCaptor.forClass(List.class);
            verify(orderMapper).insertOrderItems(itemsCap.capture());
            List<OrderItem> oItems = itemsCap.getValue();
            assertThat(oItems).hasSize(2);
            assertThat(oItems.get(0)).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getProductName,
                    OrderItem::getQty,
                    OrderItem::getUnitPriceIncl)
                    .containsExactly(
                            cartId,
                            "P1",
                            "Product 1",
                            2,
                            100);

            verify(productMapper, times(2)).decreaseStock(anyString(), anyInt(), isNull());
            verify(cartMapper).deleteCart(cartId);
            verify(gateway).send(argThat(msg -> msg.getSubject().equals(MailTemplate.ORDER_CONFIRMATION.getSubject())));

        }
    }

}
