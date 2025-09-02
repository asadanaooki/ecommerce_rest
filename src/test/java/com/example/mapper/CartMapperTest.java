package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.request.AddCartRequest;
import com.example.testUtil.FlywayResetExtension;
import com.example.testUtil.TestDataFactory;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
@Sql(scripts = {
        "/reset-cart-data.sql"
})
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
            int row = cartMapper.insertCartIfAbsent(cartId, null);
            Cart cart = cartMapper.selectCartByPrimaryKey(cartId);

            assertThat(row).isEqualTo(1);
            assertThat(cart.getCartId()).isEqualTo(cartId);
        }

        @Test
        void insertCartIfAbsent_exists() {
            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(14);
                    setUserId(null);
                }
            });
            
            int row = cartMapper.insertCartIfAbsent(cartId, null);
            assertThat(row).isEqualTo(0);
        }
    }

    @Nested
    class upsertCartItem {
        @Test
        void upsertCartItem_insert() {
            AddCartRequest req = new AddCartRequest(13);
            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(14);
                    setUserId(null);
                }
            });
            
            int row = cartMapper.upsertCartItem(new CartItem() {
                {
                    setCartId(cartId);
                    setProductId(productId);
                    setQty(req.getQty());
                    setUnitPriceExcl(300);
                }
            });

            CartItem ci = cartMapper.selectCartItemByPrimaryKey(cartId, productId);

            assertThat(row).isEqualTo(1);
            assertThat(ci.getCartId()).isEqualTo(cartId);
            assertThat(ci.getUnitPriceExcl()).isEqualTo(300);
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
            
            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(14);
                    setUserId(null);
                }
            });
            cartMapper.upsertCartItem(new CartItem() {
                {
                    setCartId(cartId);
                    setProductId(productId);
                    setQty(2);
                    setUnitPriceExcl(1300);
                }
            });
            
            int row = cartMapper.upsertCartItem(new CartItem() {
                {
                    setCartId(cartId);
                    setProductId(productId);
                    setQty(qty);
                    setUnitPriceExcl(price);
                }
            });

            CartItem ci = cartMapper.selectCartItemByPrimaryKey(cartId, productId);

            assertThat(row).isEqualTo(2);
            assertThat(ci.getCartId()).isEqualTo(cartId);
            assertThat(ci.getUnitPriceExcl()).isEqualTo(price);
            assertThat(ci.getQty()).isEqualTo(expectedQty);
            assertThat(ci.getUnitPriceIncl()).isEqualTo((price * 110) / 100);
            assertThat(ci.getSubtotalIncl()).isEqualTo(((price * 110) / 100) * expectedQty);
        }
    }

    @Nested
    class FindOrCreateCartIdByUser {
        @Test
        void findOrCreateCartIdByUser_new() {
            factory.deleteCartByUserId(userId);

            String candidate = UUID.randomUUID().toString();
            Map<String, String> p = new HashMap();
            p.put("userId", userId);
            p.put("candidateCartId", candidate);

            int rows = cartMapper.findOrCreateCartIdByUser(p);
            assertThat(rows).isEqualTo(1);
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
            factory.deleteCartByUserId(userId);
            cartMapper.insertCartIfAbsent(cartId, userId);

            String candidate = UUID.randomUUID().toString();
            Map<String, String> p = new HashMap();
            p.put("userId", userId);
            p.put("candidateCartId", candidate);

            int rows = cartMapper.findOrCreateCartIdByUser(p);
            assertThat(rows).isEqualTo(0);
            assertThat(p.get("cartId")).isEqualTo(cartId);

            Cart cart = cartMapper.selectCartByPrimaryKey(cartId);
            assertThat(cart.getCartId()).isEqualTo(cartId);
            assertThat(cart.getUserId()).isEqualTo(userId);
            assertThat(cart.getCreatedAt()).isNotNull();
            assertThat(cart.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class MergeCart {
        String userCart = "00000000-0000-0000-0000-000000000001";
        String guestCart = "00000000-0000-0000-0000-000000000002";
        String productId = "09d5a43a-d24c-41c7-af2b-9fb7b0c9e049";

        @BeforeEach
        void setup() {
            factory.deleteCartByUser(userId);
            factory.createCart(new Cart() {
                {
                    setCartId(userCart);
                    setTtlDays(14);
                    setUserId(userId);
                }
            });
            factory.createCart(new Cart() {
                {
                    setCartId(guestCart);
                    setTtlDays(14);
                }
            });
        }

        @Test
        void mergeCart_guestEmpty() {
            int rows = cartMapper.mergeCart(guestCart, userCart);
            assertThat(rows).isEqualTo(0);
        }

        @Test
        void mergeCart_new() {
            factory.createCartItem(new CartItem() {
                {
                    setCartId(guestCart);
                    setProductId(productId);
                    setQty(5);
                    setUnitPriceExcl(3000);
                }
            });
            int rows = cartMapper.mergeCart(guestCart, userCart);
            assertThat(rows).isEqualTo(1);

            CartItem item = cartMapper.selectCartItemByPrimaryKey(userCart, productId);
            assertThat(item)
                    .extracting(
                            CartItem::getCartId,
                            CartItem::getProductId,
                            CartItem::getQty,
                            CartItem::getUnitPriceExcl)
                    .containsExactly(
                            userCart,
                            productId,
                            5,
                            3000);
        }

        @ParameterizedTest
        @CsvSource({
                // existing, add, expectedQty, expectedRows
                "20, 3, 20, 1", // 更新なし
                "15, 5, 20, 2", // 20ちょうど
                "18, 6, 20, 2" // 20超過
        })
        void mergeCart_updateBoundary(int existing, int add, int expectedQty, int expectedRows) {
            factory.createCartItem(new CartItem() {
                {
                    setCartId(userCart);
                    setProductId(productId);
                    setQty(existing);
                    setUnitPriceExcl(4800);
                }
            });
            factory.createCartItem(new CartItem() {
                {
                    setCartId(guestCart);
                    setProductId(productId);
                    setQty(add);
                    setUnitPriceExcl(4800);
                }
            });

            int rows = cartMapper.mergeCart(guestCart, userCart);
            assertThat(rows).isEqualTo(expectedRows);

            CartItem item = cartMapper.selectCartItemByPrimaryKey(userCart, productId);
            assertThat(item.getQty()).isEqualTo(expectedQty);

        }
    }

    @Nested
    class selectCartItems {
        int pageSize = 3;

        @BeforeEach
        void setup() {
            factory.deleteCartByUser(userId);
            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(14);
                }
            });
        }

        @Test
        void selectCartItems_cartEmpty() {
            List<CartItemDto> items = cartMapper.selectCartItems(cartId);
            assertThat(items).isEmpty();
        }

        @Test
        void selectCartItems_cartExists() {
            LocalDateTime time1 = LocalDateTime.of(2025, 6, 22, 10, 40, 3);
            LocalDateTime time2 = LocalDateTime.of(2025, 6, 21, 15, 42, 3);

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

            List<CartItemDto> items = cartMapper.selectCartItems(cartId);

            assertThat(items).hasSize(pageSize)
                    .extracting(CartItemDto::getProductId)
                    .containsExactly("f9c9cfb2-0893-4f1c-b508-f9e909ba5274",
                            "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68",
                            "4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07");

            assertThat(items.get(0))
                    .satisfies(dto -> {
                        assertThat(dto.getProductId())
                                .isEqualTo("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
                        assertThat(dto.getProductName()).isEqualTo("Item18");
                        assertThat(dto.getQty()).isEqualTo(1);
                        assertThat(dto.getUnitPriceIncl()).isEqualTo(3630);  // 3300 * 1.1
                        assertThat(dto.getSubtotalIncl()).isEqualTo(3630);  // unit_price_incl * qty(1)
                    });
        }
    }

    @Nested
    class IsCartExpired {

        String cartId = UUID.randomUUID().toString();

        @BeforeEach
        void setup() {
            factory.freezeNow(LocalDateTime.of(2025, 7, 3, 10, 0));
        }

        @AfterEach
        void tearDown() {
            factory.unfreezeNow();
        }

        @Test
        void isCartExpired_within() {
            LocalDateTime time = LocalDateTime.of(2025, 6, 22, 10, 40, 3);

            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(14);
                }
            });
            factory.createCartItem(new CartItem() {
                {
                    setCartId(cartId);
                    setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                    setQty(1);
                    setUnitPriceExcl(750);
                    setCreatedAt(time);
                    setUpdatedAt(time);
                }
            });
            assertThat(cartMapper.isCartExpired(cartId)).isFalse();
        }

        @Test
        void isCartExpired_over() {
            LocalDateTime time = LocalDateTime.of(2024, 5, 3, 10, 40, 3);

            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(60);
                    setUserId("111e8400-e29b-41d4-a716-446655440111");
                    setCreatedAt(time);
                    setUpdatedAt(time);
                }
            });
            cartMapper.insertCartIfAbsent(cartId, "111e8400-e29b-41d4-a716-446655440111");
            factory.createCartItem(new CartItem() {
                {
                    setCartId(cartId);
                    setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                    setQty(1);
                    setUnitPriceExcl(750);
                    setCreatedAt(time);
                    setUpdatedAt(time);
                }
            });
            assertThat(cartMapper.isCartExpired(cartId)).isTrue();
        }

        @ParameterizedTest
        @CsvSource(value = {
                "2025-06-19T10:00, NULL, false",
                "2025-06-19T09:59:59, NULL, true",
                "2025-05-04T10:00, 111e8400-e29b-41d4-a716-446655440111, false",
                "2025-05-04T09:59:59, 111e8400-e29b-41d4-a716-446655440111, true"
        }, nullValues = "NULL")
        void isCartExpired_boundary(LocalDateTime time, String userId, boolean expected) {
            factory.createCart(new Cart() {
                {
                    setCartId(cartId);
                    setTtlDays(userId != null ? 60 : 14);
                    setUserId(userId);
                    setCreatedAt(time);
                    setUpdatedAt(time);
                }
            });
            factory.createCartItem(new CartItem() {
                {
                    setCartId(cartId);
                    setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                    setQty(1);
                    setUnitPriceExcl(750);
                    setCreatedAt(time);
                    setUpdatedAt(time);
                }
            });
            assertThat(cartMapper.isCartExpired(cartId)).isEqualTo(expected);
        }
    }

    @Nested
    class DeleteExpiredCarts {
        String cartA = UUID.randomUUID().toString(); // updated_at=2025/07/30 10:40:10, ttl=14, user=null -> 期限 = 8/20 10:40:10
        String cartB = UUID.randomUUID().toString(); // updated_at=2025/06/03 10:40:10, ttl=60, user=有り -> 期限 = 8/09 10:40:10
        String cartC = UUID.randomUUID().toString();
        String productId = "97113c2c-719a-490c-9979-144d92905c33";

        @BeforeEach
        void setup() {
            // A: updated_at=2025/7/30 10:40:10, userId=null, ttl_days=14 → 期限=8/20 10:40:10（=境界）
            LocalDateTime a = LocalDateTime.of(2025, 7, 30, 10, 40, 10);
            factory.createCart(new Cart() {
                {
                    setCartId(cartA);
                    setTtlDays(14);
                    setCreatedAt(a);
                    setUpdatedAt(a);
                }
            });
            factory.createCartItem(new CartItem() {
                {
                    setCartId(cartA);
                    setProductId(productId);
                    setQty(1);
                    setUnitPriceExcl(1000);
                    setCreatedAt(a);
                    setUpdatedAt(a);
                }
            });

            // B: updated_at=2025/6/3 10:40:10, userId=有り, ttl_days=60 → 期限=8/9 10:40:10（すでに期限切れ）
            LocalDateTime b = LocalDateTime.of(2025, 6, 3, 10, 40, 10);
            factory.createCart(new Cart() {
                {
                    setCartId(cartB);
                    setTtlDays(60);
                    setUserId(userId);
                    setCreatedAt(b);
                    setUpdatedAt(b);
                }
            });
            factory.createCartItem(new CartItem() {
                {
                    setCartId(cartB);
                    setProductId(productId);
                    setQty(2);
                    setUnitPriceExcl(2000);
                    setCreatedAt(b);
                    setUpdatedAt(b);
                }
            });

            // C: updated_at=2025/8/16 10:40:10, userId=null, ttl_days=14 → 期限=9/6 10:40:10（未来）
            LocalDateTime c = LocalDateTime.of(2025, 8, 16, 10, 40, 10);
            factory.createCart(new Cart() {
                {
                    setCartId(cartC);
                    setTtlDays(14);
                    setCreatedAt(c);
                    setUpdatedAt(c);
                }
            });
            factory.createCartItem(new CartItem() {
                {
                    setCartId(cartC);
                    setProductId(productId);
                    setQty(3);
                    setUnitPriceExcl(3000);
                    setCreatedAt(c);
                    setUpdatedAt(c);
                }
            });
        }

        @AfterEach
        void tearDown() {
            factory.unfreezeNow();
        }

        @Test
        void deleteExpiredCarts_equalBoundary() {
            factory.freezeNow(LocalDateTime.of(2025, 8, 20, 10, 40, 10));
            
            int deleted = cartMapper.deleteExpiredCarts();
            
            assertThat(deleted).isEqualTo(1);
            
         // 残存確認：cartAは等号なので残る、cartBは期限切れで消える、cartCは未来で残る
            assertThat(cartMapper.selectCartByPrimaryKey(cartA)).isNotNull();
            assertThat(cartMapper.selectCartByPrimaryKey(cartB)).isNull();     // 削除済
            assertThat(cartMapper.selectCartByPrimaryKey(cartC)).isNotNull();

            // カスケード確認：cartB の cart_item は削除、他は残る
            assertThat(cartMapper.selectCartItemByPrimaryKey(cartA, productId)).isNotNull();
            assertThat(cartMapper.selectCartItemByPrimaryKey(cartB, productId)).isNull(); // 削除済
            assertThat(cartMapper.selectCartItemByPrimaryKey(cartC, productId)).isNotNull();
        }
        
        @Test
        void deleteExpiredCarts_overBoundary() {
            factory.freezeNow(LocalDateTime.of(2025, 8, 20, 10, 40, 11));
            
            int deleted = cartMapper.deleteExpiredCarts();
            
            assertThat(deleted).isEqualTo(2);
            
         // 残存確認：cartAは等号なので残る、cartBは期限切れで消える、cartCは未来で残る
            assertThat(cartMapper.selectCartByPrimaryKey(cartA)).isNull();
            assertThat(cartMapper.selectCartByPrimaryKey(cartB)).isNull();     // 削除済
            assertThat(cartMapper.selectCartByPrimaryKey(cartC)).isNotNull();

            // カスケード確認：cartB の cart_item は削除、他は残る
            assertThat(cartMapper.selectCartItemByPrimaryKey(cartA, productId)).isNull();
            assertThat(cartMapper.selectCartItemByPrimaryKey(cartB, productId)).isNull(); // 削除済
            assertThat(cartMapper.selectCartItemByPrimaryKey(cartC, productId)).isNotNull();
        }
    }
}
