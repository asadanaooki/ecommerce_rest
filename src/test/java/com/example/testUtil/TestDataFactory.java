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
    
    public void deleteCart(String cartId) {
        jdbcTemplate.update("DELETE FROM cart WHERE cart_id = ?", cartId);
    }
    
    public void deleteCartItemByCartId(String cartId) {
        jdbcTemplate.update("DELETE FROM cart_item WHERE cart_id = ?", cartId);
    }

}
