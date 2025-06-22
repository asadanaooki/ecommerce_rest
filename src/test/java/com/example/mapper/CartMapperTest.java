package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.request.AddCartRequest;
import com.example.testUtil.TestDataFactory;
import com.example.util.PaginationUtil;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
class CartMapperTest {

    @Autowired
    CartMapper cartMapper;

    @Autowired
    TestDataFactory factory;

    String cartId = "bbbbeeee-cccc-dddd-aaaa-111122223333";

    String productId = "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68";
    
    String userId = "550e8400-e29b-41d4-a716-446655440000";

    @Nested
    class insertCartIfAbsent {
        @Test
        void insertCartIfAbsent_absent() {
            factory.deleteCartItemByCartId(cartId);
            factory.deleteCart(cartId);
            int row = cartMapper.insertCartIfAbsent(cartId, null);
            Cart cart = cartMapper.selectCartByPrimaryKey(cartId);

            assertThat(row).isOne();
            assertThat(cart.getCartId()).isEqualTo(cartId);
        }

        @Test
        void insertCartIfAbsent_exists() {
            int row = cartMapper.insertCartIfAbsent(cartId, null);
            assertThat(row).isZero();
        }
    }

    @Nested
    class upsertCartItem {
        @Test
        void upsertCartItem_insert() {
            AddCartRequest req = new AddCartRequest(productId, 13);
            factory.deleteCartItemByCartId(cartId);
            int row = cartMapper.upsertCartItem(cartId, req, 300);
            
            CartItem ci = cartMapper.selectCartItemByPrimaryKey(cartId, productId);

            assertThat(row).isOne();
            assertThat(ci.getCartId()).isEqualTo(cartId);
            assertThat(ci.getPrice()).isEqualTo(300);
            assertThat(ci.getQty()).isEqualTo(13);
            assertThat(ci.getCreatedAt()).isNotNull();
            assertThat(ci.getUpdatedAt()).isNotNull();
        }

        @ParameterizedTest
        @CsvSource({
                "5, 1300, 7",
                "18, 800, 20",
                "19, 200, 20"
        })
        void upsertCartItem_update(int qty, int price, int expectedQty) {
            AddCartRequest req = new AddCartRequest(productId, qty);
            int row = cartMapper.upsertCartItem(cartId, req, price);

            CartItem ci = cartMapper.selectCartItemByPrimaryKey(cartId, productId);

            assertThat(row).isEqualTo(2);
            assertThat(ci.getCartId()).isEqualTo(cartId);
            assertThat(ci.getPrice()).isEqualTo(price);
            assertThat(ci.getQty()).isEqualTo(expectedQty);
        }
    }
    @Nested
    class FindOrCreateCartIdByUser{
        @Test
        void findOrCreateCartIdByUser_new() {
            factory.deleteCartItemsByUserId(userId);
            factory.deleteCartByUserId(userId);
            
            String candidate = UUID.randomUUID().toString();
            Map<String, String> p = new HashMap();
            p.put("userId", userId);
            p.put("candidateCartId", candidate);
            
            int rows = cartMapper.findOrCreateCartIdByUser(p);
            assertThat(rows).isOne();
            assertThat(p.get("cartId")).isEqualTo(candidate);
            
           Cart cart = cartMapper.selectCartByPrimaryKey(candidate);
           assertThat(cart.getCartId()).isEqualTo(candidate);
           assertThat(cart.getUserId()).isEqualTo(userId);
           assertThat(cart.getCreatedAt()).isNotNull();
           assertThat(cart.getUpdatedAt()).isNotNull();
        }
        
        @Test
        void findOrCreateCartIdByUser_exists() {
          String cartId = UUID.randomUUID().toString();
            factory.deleteCartItemsByUserId(userId);
            factory.deleteCartByUserId(userId);
            cartMapper.insertCartIfAbsent(cartId, userId);
            
            String candidate = UUID.randomUUID().toString();
            Map<String, String> p = new HashMap();
            p.put("userId", userId);
            p.put("candidateCartId", candidate);
            
            int rows = cartMapper.findOrCreateCartIdByUser(p);
            assertThat(rows).isZero();
            assertThat(p.get("cartId")).isEqualTo(cartId);
            
           Cart cart = cartMapper.selectCartByPrimaryKey(cartId);
           assertThat(cart.getCartId()).isEqualTo(cartId);
           assertThat(cart.getUserId()).isEqualTo(userId);
           assertThat(cart.getCreatedAt()).isNotNull();
           assertThat(cart.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class MergeCart{
        String userCart = "00000000-0000-0000-0000-000000000001";
        String guestCart = "00000000-0000-0000-0000-000000000002";
        String productId = "09d5a43a-d24c-41c7-af2b-9fb7b0c9e049";
        
        @BeforeEach
        void setup() {
            factory.deleteCartByUser(userId);
            factory.createCart(guestCart, null);
            factory.createCart(userCart, userId);
        }
        
        @Test
        void mergeCart_guestEmpty() {
            int rows = cartMapper.mergeCart(guestCart, userCart);
            assertThat(rows).isZero();
        }
        
        @Test
        void mergeCart_new() {
            factory.createCartItem(new CartItem() {{
                setCartId(cartId);
                setProductId(productId);
                setQty(5);
                setPrice(3000);
            }});
            int rows = cartMapper.mergeCart(guestCart, userCart);
            assertThat(rows).isOne();
            
            CartItem item = cartMapper.selectCartItemByPrimaryKey(userCart, productId);
            assertThat(item)
            .extracting(
                    CartItem::getCartId,
                    CartItem::getProductId,
                    CartItem::getQty,
                    CartItem::getPrice)
            .containsExactly(
                    userCart,
                    productId,
                    5,
                    3000
                    );
        }
        
        @ParameterizedTest
        @CsvSource({
            // existing, add, expectedQty, expectedRows
            "20, 3, 20, 1", // 更新なし
            "15, 5, 20, 2", // 20ちょうど
            "18, 6, 20, 2" // 20超過
        })
        void mergeCart_updateBoundary(int existing, int add, int expectedQty, int expectedRows) {
            factory.createCartItem(new CartItem() {{
                setCartId(userCart);
                setProductId(productId);
                setQty(existing);
                setPrice(4800);
            }});
            factory.createCartItem(new CartItem() {{
                setCartId(guestCart);
                setProductId(productId);
                setQty(add);
                setPrice(4800);
            }});
            
            int rows = cartMapper.mergeCart(guestCart, userCart);
            assertThat(rows).isEqualTo(expectedRows);
            
            CartItem item = cartMapper.selectCartItemByPrimaryKey(userCart, productId);
            assertThat(item.getQty()).isEqualTo(expectedQty);
            
        }
    }
    
    @Nested
    class SelectCartItemsPage{
        int pageSize = 3;
        
        @BeforeEach
        void setup() {
            factory.deleteCartByUser(userId);
            factory.createCart(cartId, userId);
        }
        
        @Test
        void SelectCartItemsPage_cartEmpty() {
            List<CartItemDto> items = cartMapper.selectCartItemsPage(cartId,
                    pageSize, PaginationUtil.calculateOffset(1, pageSize));
            assertThat(items).isEmpty();
        }
        
        @Test
        void SelectCartItemsPage_cartExists() {
            LocalDateTime time1 = LocalDateTime.of(2025, 6, 22, 10, 40,3);
            LocalDateTime time2 = LocalDateTime.of(2025, 6, 21, 15, 42,3);
            
            factory.createCartItem(new CartItem() {{
                setCartId(cartId);
                setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                setQty(1);
                setPrice(750);
                setCreatedAt(time1);
                setUpdatedAt(time1);
            }});
            factory.createCartItem(new CartItem() {{
                setCartId(cartId);
                setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
                setQty(1);
                setPrice(3300);
                setCreatedAt(time1);
                setUpdatedAt(time1);
            }});
            factory.createCartItem(new CartItem() {{
                setCartId(cartId);
                setProductId("4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07");
                setQty(4);
                setPrice(1800);
                setCreatedAt(time2);
                setUpdatedAt(time2);
            }});
            
            List<CartItemDto> items = cartMapper.selectCartItemsPage(cartId,
                    pageSize, PaginationUtil.calculateOffset(1, pageSize));
            
            assertThat(items).hasSize(pageSize)
            .extracting(CartItemDto::getProductId, CartItemDto::isPriceChanged)
            .containsExactly(tuple("f9c9cfb2-0893-4f1c-b508-f9e909ba5274", true),
                    tuple("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68", false),
                    tuple("4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07", false));
            
            assertThat(items.get(0))
            .satisfies(dto ->{
                assertThat(dto.getProductId())
                .isEqualTo("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
            assertThat(dto.getProductName()).isEqualTo("Item18");
            assertThat(dto.getQty()).isEqualTo(1);
            assertThat(dto.getPriceEx()).isEqualTo(3200);
            assertThat(dto.getPriceInc()).isNull();
            assertThat(dto.isPriceChanged()).isTrue();
            });
        }
    }
}
