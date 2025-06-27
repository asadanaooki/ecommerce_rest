package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.dto.CartItemDto;
import com.example.dto.CheckoutDto;
import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.CartMapper;
import com.example.mapper.CheckoutMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.util.TaxCalculator;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    CartMapper cartMapper;

    @Mock
    UserMapper userMapper;
    
    @Mock
    ProductMapper productMapper;
    
    @Mock
    CheckoutMapper checkoutMapper;

    @Spy
    TaxCalculator calculator = new TaxCalculator(10);

    @InjectMocks
    CheckoutService checkoutService;

    @Mock
    JavaMailSender sender;

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
                    setCartId("cartId");
                    setUserId(userId);
                    setVersion(1);
                }
            };

            lenient().doReturn(cart).when(cartMapper).selectCartByUser(userId);
            lenient().doReturn(user).when(userMapper).selectUserByPrimaryKey(userId);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "testビル" })
        void loadCheckout_address(String value) {
            user.setAddressBuilding(value);
            doReturn(cart).when(cartMapper).selectCartByPrimaryKey("cartId");
            CheckoutDto dto = checkoutService.loadCheckout(userId);

            assertThat(dto.getAddress())
                    .isEqualTo("東京都千代田区丸の内1-1-1" + (value == null ? "" : value));
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
        //                            setPriceEx(100);
        //                            setPriceAtCartAddition(100);
        //                        }
        //                    },
        //                    // ② 在庫切れ品
        //                    new CartItemDto() {
        //                        {
        //                            setProductId("B-002");
        //                            setProductName("在庫切れ品");
        //                            setStatus("1"); // 販売中
        //                            setStock(0); // stock <= 0 → OUT_OF_STOCK
        //                            setQty(2);
        //                            setPriceEx(200);
        //                            setPriceAtCartAddition(200);
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
            doReturn(null).when(cartMapper).selectCartByPrimaryKey("cartId");

            assertThatThrownBy(() -> checkoutService.loadCheckout(userId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
        }

        @Test
        void loadCheckout_cartExists() {
            List<CartItemDto> items = List.of(
                    new CartItemDto() {
                        {
                            setProductId("N-005");
                            setProductName("通常品");
                            setQty(2);
                            setPriceEx(200);
                            setPriceAtCartAddition(200);
                        }
                    },
                    new CartItemDto() {
                        {
                            setProductId("N-006");
                            setProductName("通常品2");
                            setQty(1);
                            setPriceEx(100);
                            setPriceAtCartAddition(400);
                        }
                    });
            doReturn(cart).when(cartMapper).selectCartByPrimaryKey("cartId");
            doReturn(items).when(cartMapper).selectCartItems("cartId");

            CheckoutDto dto = checkoutService.loadCheckout(userId);

            assertThat(dto.getUsername()).isEqualTo("山田 太郎");
            assertThat(dto.getPostalCode()).isEqualTo("1000001");
            assertThat(dto.getAddress()).isNotBlank();

            assertThat(dto.getCart().getItems()).hasSize(2).extracting(
                    CartItemDto::getProductId,
                    CartItemDto::getProductName,
                    CartItemDto::getQty,
                    CartItemDto::getPriceInc,
                    CartItemDto::getSubtotal)
                    .containsExactly(tuple("N-005", "通常品", 2, 220, 440),
                            tuple("N-006", "通常品2", 1, 110, 110));

            assertThat(dto.getCart().getTotalPrice()).isEqualTo(550);
            assertThat(dto.getCart().getTotalQty()).isEqualTo(3);
        }

        //        @Test
        //        void loadCheckout_mixedItems() {
        //            List<CartItemDto> mixed = List.of(
        //                    // ① 販売停止
        //                    new CartItemDto() {{
        //                        setProductId("D-001"); setProductName("販売停止品");
        //                        setStatus("0");            // STATUS_DISCONTINUED
        //                        setStock(10); setQty(1);
        //                        setPriceEx(100); setPriceAtCartAddition(100);
        //                    }},
        //                    // ③ 在庫不足
        //                    new CartItemDto() {{
        //                        setProductId("L-003"); setProductName("在庫不足品");
        //                        setStatus("1");
        //                        setStock(1);  setQty(3);   // stock < qty
        //                        setPriceEx(120); setPriceAtCartAddition(120);
        //                    }},
        //                    // ④ 価格改定
        //                    new CartItemDto() {{
        //                        setProductId("P-004"); setProductName("価格改定品");
        //                        setStatus("1");
        //                        setStock(5);  setQty(1);
        //                        setPriceEx(150); setPriceAtCartAddition(100); // 価格改定
        //                    }},
        //                    // ⑤ 正常品
        //                    new CartItemDto() {{
        //                        setProductId("N-005"); setProductName("通常品");
        //                        setStatus("1");
        //                        setStock(20); setQty(2);
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
        //            assertThat(dto.getTotalPrice()).isEqualTo(1001);
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
        //                        setStatus("1"); setStock(10); setQty(1);
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

        @Test
        void checkout_cartVersionMismatch() {
            int passedVer = 1; // テスト入力
            int currentVer = 2; // DB 上のバージョン（≠ passedVer）

            Cart cart = new Cart() {
                {
                    setCartId(cartId);
                    setUserId(userId);
                    setVersion(currentVer);
                }
            };
            doReturn(cart).when(cartMapper).selectCartByUser(userId);

            assertThatThrownBy(() -> checkoutService.checkout(userId, passedVer))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT);
        }

        @Test
        void checkout_diffItemsAllKinds() {
            // diff が発生する 4 件分の CartItemDto
            List<CartItemDto> diffItems = List.of(
                    // ① 販売停止
                    new CartItemDto() {
                        {
                            setProductId("D-001");
                            setProductName("販売停止品");
                            setStatus("0"); // DISCONTINUED
                            setStock(10);
                            setQty(1);
                            setPriceEx(100);
                            setPriceAtCartAddition(100);
                        }
                    },
                    // ② 在庫切れ
                    new CartItemDto() {
                        {
                            setProductId("O-002");
                            setProductName("在庫切れ品");
                            setStatus("1");
                            setStock(0);
                            setQty(1); // stock <= 0
                            setPriceEx(200);
                            setPriceAtCartAddition(200);
                        }
                    },
                    // ③ 在庫不足
                    new CartItemDto() {
                        {
                            setProductId("L-003");
                            setProductName("在庫不足品");
                            setStatus("1");
                            setStock(1);
                            setQty(3); // stock < qty
                            setPriceEx(120);
                            setPriceAtCartAddition(120);
                        }
                    },
                    // ④ 価格改定
                    new CartItemDto() {
                        {
                            setProductId("P-004");
                            setProductName("価格改定品");
                            setStatus("1");
                            setStock(5);
                            setQty(1);
                            setPriceEx(150);
                            setPriceAtCartAddition(100); // 価格改定
                        }
                    });
            doReturn(new Cart() {
                {
                    setCartId(cartId);
                    setUserId(userId);
                    setVersion(2);
                }
            }).when(cartMapper).selectCartByUser(userId);
            doReturn(diffItems).when(checkoutMapper).selectCheckoutItems(cartId);

            assertThatThrownBy(() -> checkoutService.checkout(userId, 2))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT);

            assertThat(diffItems).extracting(CartItemDto::getReason)
                    .containsExactly(
                            CartItemDto.DiffReason.DISCONTINUED,
                            CartItemDto.DiffReason.OUT_OF_STOCK,
                            CartItemDto.DiffReason.LOW_STOCK,
                            CartItemDto.DiffReason.PRICE_CHANGED);
        }

        @Test
        void checkout_success() throws MessagingException {
            Cart cart = new Cart() {
                {
                    setCartId(cartId);
                    setUserId(userId);
                    setVersion(2);
                }
            };
            List<CartItemDto> items = List.of(
                    new CartItemDto() {
                        { // 通常品1
                            setProductId("N-001");
                            setProductName("通常品1");
                            setStatus("1");
                            setStock(10);
                            setQty(2);
                            setPriceEx(200);
                            setPriceAtCartAddition(200);
                        }
                    },
                    new CartItemDto() {
                        { // 通常品2
                            setProductId("N-002");
                            setProductName("通常品2");
                            setStatus("1");
                            setStock(5);
                            setQty(1);
                            setPriceEx(100);
                            setPriceAtCartAddition(100);
                        }
                    });
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
            doReturn(items).when(checkoutMapper).selectCheckoutItems(cartId);
            doReturn(user).when(userMapper).selectUserByPrimaryKey(userId);
            doReturn(mock(MimeMessage.class)).when(sender).createMimeMessage();
            
            checkoutService.checkout(userId, 2);
            
            ArgumentCaptor<Order> headerCap = ArgumentCaptor.forClass(Order.class);
            verify(checkoutMapper).insertOrderHeader(headerCap.capture());
            Order header = headerCap.getValue();
            assertThat(header).extracting(
                    Order::getOrderId,
                    Order::getUserId,
                    Order::getName,
                    Order::getPostalCode,
                    Order::getAddress,
                    Order::getTotalQty,
                    Order::getTotalPrice)
            .containsExactly(
                    cartId,
                    userId,
                    "山田 太郎",
                    "1000001",
                    "東京都千代田区丸の内1-1-1",
                    3,
                    550
                    );
            
            ArgumentCaptor<List<OrderItem>> itemsCap = ArgumentCaptor.forClass(List.class);
            verify(checkoutMapper).insertOrderItems(itemsCap.capture());
            List<OrderItem> oItems = itemsCap.getValue();
            assertThat(oItems).hasSize(2);
            assertThat(oItems.get(0)).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty,
                    OrderItem::getPrice,
                    OrderItem::getSubtotal
                    )
            .containsExactly(
                    cartId,
                    "N-001",
                    2,
                    220,
                    440
                    );
            
            verify(productMapper,times(2)).decreaseStock(anyString(), anyInt());
            verify(cartMapper).deleteCart(cartId);
        }
    }

}
