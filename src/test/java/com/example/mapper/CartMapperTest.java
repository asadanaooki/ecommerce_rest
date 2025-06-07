package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.request.AddCartRequest;
import com.example.testUtil.TestDataFactory;

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

    @Nested
    class insertCartIfAbsent {
        @Test
        void insertCartIfAbsent_absent() {
            factory.deleteCartItemByCartId(cartId);
            factory.deleteCart(cartId);
            int row = cartMapper.insertCartIfAbsent(cartId);
            Cart cart = cartMapper.selectCartByPrimaryKey(cartId);

            assertThat(row).isOne();
            assertThat(cart.getCartId()).isEqualTo(cartId);
        }

        @Test
        void insertCartIfAbsent_exists() {
            int row = cartMapper.insertCartIfAbsent(cartId);
            assertThat(row).isZero();
        }
    }

    @Nested
    class upsertCartItem {
        @Test
        void upsertCartItem_insert() {
            AddCartRequest req = new AddCartRequest(productId, 13, 315);
            factory.deleteCartItemByCartId(cartId);
            int row = cartMapper.upsertCartItem(cartId, req);
            
            CartItem ci = cartMapper.selectCartItemByPrimaryKey(cartId, productId);

            assertThat(row).isOne();
            assertThat(ci.getCartId()).isEqualTo(cartId);
            assertThat(ci.getPriceIncTax()).isEqualTo(315);
            assertThat(ci.getQty()).isEqualTo(13);
            assertThat(ci.getCreatedAt()).isNotNull();
            assertThat(ci.getUpdatedAt()).isNotNull();
        }

        @ParameterizedTest
        @CsvSource({
                "5, 1300, 7",
                "18, 825, 20",
                "19, 200, 20"
        })
        void upsertCartItem_update(int qty, int price, int expectedQty) {
            AddCartRequest req = new AddCartRequest(productId, qty, price);
            int row = cartMapper.upsertCartItem(cartId, req);

            CartItem ci = cartMapper.selectCartItemByPrimaryKey(cartId, productId);

            assertThat(row).isEqualTo(2);
            assertThat(ci.getCartId()).isEqualTo(cartId);
            assertThat(ci.getPriceIncTax()).isEqualTo(price);
            assertThat(ci.getQty()).isEqualTo(expectedQty);
        }
    }

}
