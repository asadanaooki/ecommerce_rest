package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.entity.CartItem;
import com.example.entity.Product;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartMapper cartMapper;

    @Mock
    ProductMapper productMapper;


    @InjectMocks
    CartService cartService;

    @Nested
    class ShowCart {
        String cartId = UUID.randomUUID().toString();

        String userId = "user";

        @Test
        void showCart_withoutCart() {
            doReturn(Collections.EMPTY_LIST).when(cartMapper).selectCartItems(cartId);
            CartDto dto = cartService.showCart(cartId);

            assertThat(dto.getItems()).isEmpty();
            assertThat(dto.getTotalQty()).isZero();
            assertThat(dto.getTotalPrice()).isZero();
        }

        @Test
        void showCart_cartExists() {
            doReturn(List.of(new CartItemDto())).when(cartMapper).selectCartItems(cartId);
            CartDto dto = cartService.showCart(cartId);
            assertThat(dto.getItems()).isNotEmpty();
        }

        @Test
        void showCart_multipleItems() {
            List<CartItemDto> items = List.of(
                    new CartItemDto() {
                        {
                            setProductId("A-001");
                            setProductName("Item A");
                            setQty(3);
                            setPriceEx(100); // 税抜 100 円
                        }
                    },
                    new CartItemDto() {
                        {
                            setProductId("B-002");
                            setProductName("Item B");
                            setQty(1);
                            setPriceEx(200); // 税抜 200 円
                            setSubtotal(220); // 220 * 1
                        }
                    });
            doReturn(items).when(cartMapper).selectCartItems(anyString());

            CartDto dto = cartService.showCart(cartId);

            assertThat(dto.getTotalQty()).isEqualTo(4);
            assertThat(dto.getTotalPrice()).isEqualTo(500);

            assertThat(dto.getItems()).hasSize(2).first()
                    .extracting(
                            CartItemDto::getProductId,
                            CartItemDto::getProductName,
                            CartItemDto::getQty,
                            CartItemDto::getPriceEx,
                            CartItemDto::getSubtotal)
                    .containsExactly(
                            "A-001",
                            "Item A",
                            3,
                            100,
                            300);
            assertThat(dto.getItems().get(1).getProductId()).isEqualTo("B-002");

        }
    }

    @Nested
    class AddToCart {
        String candidate = "cand-123";
        String productId = "P-001";
        String userId = "user-1";
        AddCartRequest req;
        Product p;

        @BeforeEach
        void setup() {
            req = new AddCartRequest();
            req.setQty(2);

            p = new Product();
            p.setPrice(1000);
            doReturn(p).when(productMapper).selectByPrimaryKey(productId);
        }

        @Test
        void addToCart_notExpired() {
            doReturn(false).when(cartMapper).isCartExpired(candidate);

            Optional<String> result = cartService.addToCart(candidate, userId, productId, req);

            ArgumentCaptor<CartItem> cap = ArgumentCaptor.forClass(CartItem.class);
            verify(cartMapper).upsertCartItem(cap.capture());
            verify(cartMapper).insertCartIfAbsent(candidate, userId);
            assertThat(result).isEmpty();

            CartItem ci = cap.getValue();
            assertThat(ci).extracting(
                    CartItem::getCartId,
                    CartItem::getProductId,
                    CartItem::getQty,
                    CartItem::getPrice)
                    .containsExactly(
                            candidate,
                            productId,
                            2,
                            1000);
        }

        @Test
        void addToCart_expired() {
            doReturn(true).when(cartMapper).isCartExpired(candidate);
            String newId = "00000000-0000-0000-0000-000000000001";
            UUID u = UUID.fromString(newId);
            try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
                mocked.when(UUID::randomUUID).thenReturn(u);

                Optional<String> result = cartService.addToCart(candidate, userId, productId, req);

                ArgumentCaptor<CartItem> cap = ArgumentCaptor.forClass(CartItem.class);
                verify(cartMapper).upsertCartItem(cap.capture());
                verify(cartMapper).insertCartIfAbsent(newId, userId);
                assertThat(result).contains(newId);

                CartItem ci = cap.getValue();
                assertThat(ci).extracting(
                        CartItem::getCartId,
                        CartItem::getProductId,
                        CartItem::getQty,
                        CartItem::getPrice)
                        .containsExactly(
                                newId,
                                productId,
                                2,
                                1000);
            }
        }
    }

    @Nested
    class ChangeQty {
        String cartId = "cart-123";
        String productId = "P-001";
        String userId = "user-1";
        Product p;

        @BeforeEach
        void setup() {
            p = new Product();
            p.setPrice(1500);
            doReturn(p).when(productMapper).selectByPrimaryKey(productId);
        }

        @Test
        void changeQty() {
            cartService.changeQty(cartId, userId, productId, 5);

            ArgumentCaptor<CartItem> cap = ArgumentCaptor.forClass(CartItem.class);
            verify(cartMapper).upsertCartItem(cap.capture());
            verify(productMapper).selectByPrimaryKey(productId);

            CartItem ci = cap.getValue();
            assertThat(ci).extracting(
                    CartItem::getCartId,
                    CartItem::getProductId,
                    CartItem::getQty,
                    CartItem::getPrice)
                    .containsExactly(
                            cartId,
                            productId,
                            5,
                            1500);
        }
    }

    @Nested
    class RemoveItem {
        String cartId = "cart-123";
        String productId = "P-001";

        @Test
        void removeItem() {
            cartService.removeItem(cartId, productId);

            verify(cartMapper).deleteCartItem(cartId, productId);
        }
    }

}
