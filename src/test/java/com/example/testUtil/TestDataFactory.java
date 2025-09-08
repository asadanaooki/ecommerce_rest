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
import com.example.enums.order.RejectReason;
import com.example.enums.review.ReviewStatus;

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
        StringBuilder cols = new StringBuilder("cart_id, product_id, qty, unit_price_excl");
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

    /** 指定 productId & userId のレビューを1件取得 */
    public Review findReview(String productId, String userId) {
        String sql = "SELECT * FROM review WHERE product_id = ? AND user_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Review r = new Review();
            r.setProductId(rs.getString("product_id"));
            r.setUserId(rs.getString("user_id"));
            r.setRating((Integer) rs.getObject("rating"));
            r.setTitle(rs.getString("title"));
            r.setReviewText(rs.getString("review_text"));
            // Enum系は name() 保存前提
            String status = rs.getString("status");
            if (status != null)
                r.setStatus(Enum.valueOf(ReviewStatus.class, status));
            String rejectReason = rs.getString("reject_reason");
            if (rejectReason != null)
                r.setRejectReason(Enum.valueOf(RejectReason.class, rejectReason));
            r.setRejectNote(rs.getString("reject_note"));
            Timestamp cAt = rs.getTimestamp("created_at");
            if (cAt != null)
                r.setCreatedAt(cAt.toLocalDateTime());
            Timestamp uAt = rs.getTimestamp("updated_at");
            if (uAt != null)
                r.setUpdatedAt(uAt.toLocalDateTime());
            return r;
        }, productId, userId);
    }

    public int createReview(Review r) {
        StringBuilder cols = new StringBuilder("product_id, user_id, rating");
        StringBuilder marks = new StringBuilder("?, ?, ?");
        List<Object> params = new ArrayList<>(List.of(
                r.getProductId(),
                r.getUserId(),
                r.getRating()));

        // title
        if (r.getTitle() != null) {
            cols.append(", title");
            marks.append(", ?");
            params.add(r.getTitle());
        }

        // review_text
        if (r.getReviewText() != null) {
            cols.append(", review_text");
            marks.append(", ?");
            params.add(r.getReviewText());
        }

        // status (Enum → DB定義に応じて name() / ordinal())
        if (r.getStatus() != null) {
            cols.append(", status");
            marks.append(", ?");
            params.add(r.getStatus().name());
        }

        // reject_reason (Enum)
        if (r.getRejectReason() != null) {
            cols.append(", reject_reason");
            marks.append(", ?");
            params.add(r.getRejectReason().name());
        }

        // reject_note
        if (r.getRejectNote() != null) {
            cols.append(", reject_note");
            marks.append(", ?");
            params.add(r.getRejectNote());
        }

        // created_at
        if (r.getCreatedAt() != null) {
            cols.append(", created_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(r.getCreatedAt()));
        }

        // updated_at
        if (r.getUpdatedAt() != null) {
            cols.append(", updated_at");
            marks.append(", ?");
            params.add(Timestamp.valueOf(r.getUpdatedAt()));
        }

        String sql = String.format("INSERT INTO review (%s) VALUES (%s)", cols, marks);
        return jdbcTemplate.update(sql, params.toArray());
    }

    public void createProduct(Product product) {
        StringBuilder cols = new StringBuilder();
        StringBuilder marks = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // version を必須で追加
        cols.append("product_id, product_name, product_description, price_excl, stock, reserved, status, version");
        marks.append("?, ?, ?, ?, ?, ?, ?, ?");
        params.add(product.getProductId());
        params.add(product.getProductName());
        params.add(product.getProductDescription());
        params.add(product.getPriceExcl());
        params.add(product.getStock());
        params.add(product.getReserved());
        params.add(product.getStatus().name());
        params.add(product.getVersion()); // ← 新規追加

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
                "order_id, user_id, name, postal_code, address, total_qty, items_subtotal_incl, shipping_fee_incl,"
                + "cod_fee_incl");
        StringBuilder marks = new StringBuilder(
                "?, ?, ?, ?, ?, ?, ?, ?,?");
        List<Object> params = new ArrayList<>(List.of(
                order.getOrderId(),
                order.getUserId(),
                order.getName(),
                order.getPostalCode(),
                order.getAddress(),
                order.getTotalQty(),
                order.getItemsSubtotalIncl(),
                order.getShippingFeeIncl(),
                order.getCodFeeIncl()));

        if (order.getOrderNumber() > 0) {
            cols.append(", order_number");
            marks.append(", ?");
            params.add(order.getOrderNumber());
        }

        // order_status が設定されていれば追加
        if (order.getOrderStatus() != null) {
            cols.append(", order_status");
            marks.append(", ?");
            params.add(order.getOrderStatus().name());
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
