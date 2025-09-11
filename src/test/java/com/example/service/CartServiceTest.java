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
import org.springframework.test.util.ReflectionTestUtils;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.entity.CartItem;
import com.example.entity.Product;
import com.example.feature.product.ProductGuard;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartMapper cartMapper;

    @Mock
    ProductMapper productMapper;

    @Mock
    ProductGuard productGuard;

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
            assertThat(dto.getShippingFeeIncl()).isEqualTo(0);
            assertThat(dto.getItemsSubtotalIncl()).isEqualTo(0);
            assertThat(dto.getCodFeeIncl()).isNull();
            assertThat(dto.getGrandTotalIncl()).isEqualTo(0);
        }

        @Test
        void showCart_cartExists() {
            doReturn(List.of(new CartItemDto())).when(cartMapper).selectCartItems(cartId);
            CartDto dto = cartService.showCart(cartId);
            assertThat(dto.getItems()).isNotEmpty();
        }

        @Test
        void showCart_multipleItems() {
            // Create properly populated CartItemDto objects
            CartItemDto item1 = new CartItemDto();
            item1.setProductId("A-001");
            item1.setProductName("Item A");
            item1.setQty(3);
            item1.setUnitPriceIncl(1100);
            item1.setSubtotalIncl(3300);  // 3 * 110
            
            CartItemDto item2 = new CartItemDto();
            item2.setProductId("B-002");
            item2.setProductName("Item B");
            item2.setQty(1);
            item2.setUnitPriceIncl(220);
            item2.setSubtotalIncl(220);  // 1 * 220
            
            List<CartItemDto> items = List.of(item1, item2);
            doReturn(items).when(cartMapper).selectCartItems(anyString());

            CartDto dto = cartService.showCart(cartId);

            assertThat(dto.getTotalQty()).isEqualTo(4);
            assertThat(dto.getItemsSubtotalIncl()).isEqualTo(3520);
            assertThat(dto.getShippingFeeIncl()).isEqualTo(500);
            assertThat(dto.getCodFeeIncl()).isNull();
            assertThat(dto.getGrandTotalIncl()).isEqualTo(4020);

            assertThat(dto.getItems()).hasSize(2).first()
                    .extracting(
                            CartItemDto::getProductId,
                            CartItemDto::getProductName,
                            CartItemDto::getQty,
                            CartItemDto::getUnitPriceIncl,
                            CartItemDto::getSubtotalIncl)
                    .containsExactly(
                            "A-001",
                            "Item A",
                            3,
                            1100,
                            3300);
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
            p.setPriceExcl(1000);
            doReturn(p).when(productGuard).require(productId);
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
                    CartItem::getUnitPriceExcl)
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
                        CartItem::getUnitPriceExcl)
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
            p.setPriceExcl(1500);
            doReturn(p).when(productGuard).require(productId);
        }

        @Test
        void changeQty() {
            cartService.changeQty(cartId, userId, productId, 5);

            ArgumentCaptor<CartItem> cap = ArgumentCaptor.forClass(CartItem.class);
            verify(cartMapper).upsertCartItem(cap.capture());
            verify(productGuard).require(productId);

            CartItem ci = cap.getValue();
            assertThat(ci).extracting(
                    CartItem::getCartId,
                    CartItem::getProductId,
                    CartItem::getQty,
                    CartItem::getUnitPriceExcl)
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
