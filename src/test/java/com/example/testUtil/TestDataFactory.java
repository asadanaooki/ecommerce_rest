package com.example.testUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {
    /*    TODO:
    ・各mapperに汎用CRUD作成する(MybatisGeneratorのようなメソッド)
    */

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    public void updateProductStock(String productId, int stock) {
        jdbcTemplate.update(
                "UPDATE product SET stock = ? WHERE product_id = ?",
                stock,
                productId);
    }

    public void createReview(String userId, String productId, int rating) {
        jdbcTemplate.update(
                "INSERT INTO review (user_id, product_id, rating) VALUES (?, ?, ?)",
                userId, productId, rating);
    }
    
    public void createFavorite(String userId, String productId) {
        jdbcTemplate.update(
                "INSERT INTO favorite (user_id, product_id) VALUES (?, ?)", userId, productId);
    }
    
    public void createCart(String cartId, String userId) {
        String sql = (userId == null)
                ? "insert into cart (cart_id) values (?)"
                        : "insert into cart (cart_id, user_id) values (?, ?)";
        if (userId == null) {
            jdbcTemplate.update(sql, cartId);
        } else {
            jdbcTemplate.update(sql, cartId, userId);
        }
    }
    
    public void createCartItem(String cartId, String productId, int qty, int price) {
        String sql = """
                insert into cart_item (
                    cart_id,
                    product_id,
                    qty,
                    price
                ) values(?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, cartId, productId, qty, price);
    }
    
    public void deleteCart(String cartId) {
        jdbcTemplate.update("DELETE FROM cart WHERE cart_id = ?", cartId);
    }
    
    public void deleteCartByUser(String userId) {
        jdbcTemplate.update("DELETE FROM cart WHERE user_id = ?", userId);
    }
    
    public void deleteCartItem(String cartId) {
        jdbcTemplate.update("DELETE FROM cart WHERE cart_id = ?", cartId);
    }
    
    public void deleteCartItemByCartId(String cartId) {
        jdbcTemplate.update("DELETE FROM cart_item WHERE cart_id = ?", cartId);
    }
    

    /** 指定ユーザーの cart_item をすべて削除（子テーブル） */
    public int deleteCartItemsByUserId(String userId) {
        String sql = """
            DELETE ci
            FROM  cart_item AS ci
            JOIN  cart      AS c ON ci.cart_id = c.cart_id
            WHERE c.user_id = ?
        """;
        return jdbcTemplate.update(sql, userId);
    }

    /** 指定ユーザーの cart を削除（親テーブル） */
    public int deleteCartByUserId(String userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }

}
