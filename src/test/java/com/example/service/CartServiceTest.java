package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.util.CookieUtil;
import com.example.util.TaxCalculator;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartMapper cartMapper;

    @Mock
    ProductMapper productMapper;

    @Spy
    TaxCalculator calculator = new TaxCalculator(10);

    @InjectMocks
    CartService cartService;

    @Mock
    CookieUtil cookieUtil;

    @Nested
    class ShowCart {
        MockHttpServletRequest req;

        @BeforeEach
        void setup() {
            req = new MockHttpServletRequest();
        }

        @Nested
        class Guest {
            @Test
            void showCart_withoutCart() {
                doReturn(Optional.empty()).when(cookieUtil).extractCartId(req);
                CartDto dto = cartService.showCart(1, null, req);

                assertThat(dto.getItems()).isEmpty();
                assertThat(dto.getTotalQty()).isZero();
                assertThat(dto.getTotalPrice()).isZero();
            }

            @Test
            void showCart_cartExists() {
                doReturn(Optional.of("cartId")).when(cookieUtil).extractCartId(req);
                CartDto dto = cartService.showCart(1, null, req);
                assertThat(dto).isNotNull();
            }
        }

        @Nested
        class User {
            String userId = "user";

            @Test
            void showCart_withoutCart() {
                doReturn(null).when(cartMapper).selectCartByUser(userId);
                CartDto dto = cartService.showCart(1, userId, req);

                assertThat(dto.getItems()).isEmpty();
                assertThat(dto.getTotalQty()).isZero();
                assertThat(dto.getTotalPrice()).isZero();
            }

            @Test
            void showCart_multipleItems() {
                doReturn(new Cart() {
                    {
                        setCartId("cartId");
                    }
                }).when(cartMapper).selectCartByUser(userId);
                List<CartItemDto> items = List.of(
                        new CartItemDto() {
                            {
                                setProductId("A-001");
                                setProductName("Item A");
                                setQty(3);
                                setPriceEx(100); // 税抜 100 円
                                setPriceChanged(false);
                            }
                        },
                        new CartItemDto() {
                            {
                                setProductId("B-002");
                                setProductName("Item B");
                                setQty(1);
                                setPriceEx(200); // 税抜 200 円
                                setSubtotal(220); // 220 * 1
                                setPriceChanged(true);
                            }
                        });
                doReturn(items).when(cartMapper).selectCartItems(anyString());

                CartDto dto = cartService.showCart(1, userId, req);

                assertThat(dto.getTotalQty()).isEqualTo(4);
                assertThat(dto.getTotalPrice()).isEqualTo(550);

                assertThat(dto.getItems()).hasSize(2).first()
                        .extracting(
                                CartItemDto::getProductId,
                                CartItemDto::getProductName,
                                CartItemDto::getQty,
                                CartItemDto::getPriceEx,
                                CartItemDto::getPriceInc,
                                CartItemDto::getSubtotal,
                                CartItemDto::isPriceChanged)
                        .containsExactly(
                                "A-001",
                                "Item A",
                                3,
                                100,
                                110,
                                330,
                                false);
                assertThat(dto.getItems().get(1).getProductId()).isEqualTo("B-002");

            }
        }
    }

}
