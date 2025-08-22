package com.example.testUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.entity.Review;

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

    public void createFavorite(String userId, String productId) {
        jdbcTemplate.update(
                "INSERT INTO favorite (user_id, product_id) VALUES (?, ?)", userId, productId);
    }

    public void createCart(Cart cart) {
        StringBuilder cols = new StringBuilder("cart_id, ttl_days");
        StringBuilder marks = new StringBuilder("?, ?");
        List<Object> params = new ArrayList<>(
                List.of(cart.getCartId(), cart.getTtlDays()));

        if (cart.getUserId() != null) {
            cols.append(", user_id");
            marks.append(", ?");
            params.add(cart.getUserId());
        }
        if (cart.getCreatedAt() != null) {
            cols.append(", created_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(cart.getCreatedAt()));
        }
        if (cart.getUpdatedAt() != null) {
            cols.append(", updated_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(cart.getUpdatedAt()));
        }

        String sql = String.format("insert into cart (%s) values (%s)", cols, marks);
        jdbcTemplate.update(sql, params.toArray());
    }


    public void createCartItem(CartItem item) {
        StringBuilder cols = new StringBuilder("cart_id, product_id, qty, price");
        StringBuilder marks = new StringBuilder("?, ?, ?, ?");
        List<Object> params = new ArrayList<>(
                List.of(
                        item.getCartId(),
                        item.getProductId(),
                        item.getQty(),
                        item.getUnitPriceExcl()));

        if (item.getCreatedAt() != null) {
            cols.append(", created_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(item.getCreatedAt()));
        }
        if (item.getUpdatedAt() != null) {
            cols.append(", updated_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(item.getUpdatedAt()));
        }

        String sql = String.format("insert into cart_item (%s) values (%s)",
                cols, marks);
        jdbcTemplate.update(sql, params.toArray());
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

    /** 指定ユーザーの cart を削除（親テーブル） */
    public int deleteCartByUserId(String userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    /** 商品 ID をキーにレビューを一括削除 */
    public int deleteReviewsByProductId(String productId) {
        String sql = "DELETE FROM review WHERE product_id = ?";
        return jdbcTemplate.update(sql, productId);
    }

    public void createReview(Review r) {
        StringBuilder cols = new StringBuilder("product_id, user_id, rating");
        StringBuilder marks = new StringBuilder("?, ?, ?");
        List<Object> params = new ArrayList<Object>(List.of(r.getProductId(), r.getUserId(), r.getRating()));

        if (r.getReviewText() != null) {
            cols.append(", review_text");
            marks.append(", ?");
            params.add(r.getReviewText());
        }
        if (r.getCreatedAt() != null) {
            cols.append(", created_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(r.getCreatedAt()));
        }
        if (r.getUpdatedAt() != null) {
            cols.append(", updated_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(r.getUpdatedAt()));
        }
        String sql = String.format("INSERT INTO review (%s) VALUES (%s)", cols, marks);
        jdbcTemplate.update(sql, params.toArray());
    }

    public void createProduct(Product product) {
        StringBuilder cols = new StringBuilder();
        StringBuilder marks = new StringBuilder();
        List<Object> params = new ArrayList<>();

        cols.append("product_id, product_name, product_description, price, stock, reserved, status");
        marks.append("?, ?, ?, ?, ?, ?, ?");
        params.add(product.getProductId());
        params.add(product.getProductName());
        params.add(product.getProductDescription());
        params.add(product.getPriceExcl());
        params.add(product.getStock());
        params.add(product.getReserved());
        params.add(product.getStatus().name());

        if (product.getSku() > 0) {
            cols.append(", sku");
            marks.append(", ?");
            params.add(product.getSku());
        }

        // 任意カラム
        if (product.getCreatedAt() != null) {
            cols.append(", created_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(product.getCreatedAt()));
        }
        if (product.getUpdatedAt() != null) {
            cols.append(", updated_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(product.getUpdatedAt()));
        }

        String sql = String.format("INSERT INTO product (%s) VALUES (%s)", cols, marks);
        jdbcTemplate.update(sql, params.toArray());
    }

    public void createOrder(Order order) {
        // まず必須のカラムをセット
        StringBuilder cols = new StringBuilder(
                "order_id, user_id, name, postal_code, address, total_qty, total_price");
        StringBuilder marks = new StringBuilder(
                "?, ?, ?, ?, ?, ?, ?");
        List<Object> params = new ArrayList<>(List.of(
                order.getOrderId(),
                order.getUserId(),
                order.getName(),
                order.getPostalCode(),
                order.getAddress(),
                order.getTotalQty(),
                order.getTotalPriceIncl()));

        if (order.getOrderNumber() > 0) {
            cols.append(", order_number");
            marks.append(", ?");
            params.add(order.getOrderNumber());
        }

        // shipping_status が設定されていれば追加
        if (order.getShippingStatus() != null) {
            cols.append(", shipping_status");
            marks.append(", ?");
            params.add(order.getShippingStatus().name());
        }
        // payment_status が設定されていれば追加
        if (order.getPaymentStatus() != null) {
            cols.append(", payment_status");
            marks.append(", ?");
            params.add(order.getPaymentStatus().name());
        }

        // created_at と updated_at は必ず指定
        cols.append(", created_at, updated_at");
        marks.append(", ?, ?");
        params.add(Timestamp.valueOf(order.getCreatedAt()));
        params.add(Timestamp.valueOf(order.getUpdatedAt()));

        // バッククオートで table 名を囲む点に注意
        String sql = String.format(
                "INSERT INTO `order` (%s) VALUES (%s)",
                cols.toString(),
                marks.toString());
        jdbcTemplate.update(sql, params.toArray());
    }

    public void freezeNow(LocalDateTime time) {
        String formatted = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        jdbcTemplate.execute("set timestamp = unix_timestamp('" + formatted + "')");
    }

    public void unfreezeNow() {
        jdbcTemplate.execute("set timestamp = 0");
    }

}
