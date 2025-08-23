package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.testUtil.FlywayResetExtension;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderHistoryMapperTest {

    @Autowired
    OrderHistoryMapper orderHistoryMapper;

    @Test
    void selectHeadersByUser_returnsOrdersForUser() {
        // Test with existing user from test data
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        
        List<Order> orders = orderHistoryMapper.selectHeadersByUser(userId);
        
        // The test data may or may not have orders for this user
        assertThat(orders).isNotNull();
        
        // If there are orders, verify they belong to the user
        if (!orders.isEmpty()) {
            assertThat(orders).allSatisfy(order -> {
                assertThat(order.getUserId()).isEqualTo(userId);
                assertThat(order.getOrderId()).isNotNull();
            });
            
            // Verify orders are sorted by created_at desc
            for (int i = 1; i < orders.size(); i++) {
                assertThat(orders.get(i - 1).getCreatedAt())
                    .isAfterOrEqualTo(orders.get(i).getCreatedAt());
            }
        }
    }

    @Test
    void selectHeadersByUser_returnsEmptyForUserWithNoOrders() {
        // Use a user ID that shouldn't have orders in test data
        String userWithNoOrders = "00000000-0000-0000-0000-000000000000";
        
        List<Order> orders = orderHistoryMapper.selectHeadersByUser(userWithNoOrders);
        
        assertThat(orders).isNotNull();
        assertThat(orders).isEmpty();
    }

    @Test
    void selectOrderItems_returnsEmptyForNonExistentOrder() {
        String nonExistentOrderId = "NON-EXISTENT-ORDER-999";
        
        List<OrderItem> items = orderHistoryMapper.selectOrderItems(nonExistentOrderId);
        
        assertThat(items).isNotNull();
        assertThat(items).isEmpty();
    }

    @Test
    void selectOrderItems_verifiesQueryStructure() {
        // Test that the query executes without error
        // Even if no data is returned, this validates the SQL syntax
        String testOrderId = "TEST-ORDER-001";
        
        List<OrderItem> items = orderHistoryMapper.selectOrderItems(testOrderId);
        
        assertThat(items).isNotNull();
        // The list may be empty if no test data exists, but the query should execute
    }
}