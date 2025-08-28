package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.CheckoutItemDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.feature.order.OrderState;

@Mapper
public interface OrderMapper {
    /*
     * TODO:
     * deleteRemovedItemsは購入不可商品判定を確認画面に入れるときに必要かも
     * 小規模やから、注文関連はすべてこのmapperに定義
     */

    // SELECT
    List<CheckoutItemDto> selectCheckoutItems(String cartId);
    
    Order selectOrderByPrimaryKey(String orderId);
    
    List<Order> selectOrdersByUserId(String userId);
    
    List<OrderItem> selectOrderItems(String orderId);

    // INSERT
    int insertOrderHeader(Order header);
    
    int insertOrderItems(List<OrderItem> items);

    // UPDATE
    int applyTransition(String orderId, OrderState expected, OrderState next);
    
    int restoreInventory(String orderId);

    // DELETE（コメントアウト）
    // int deleteRemovedItems(String cartId, List<String> productIds);

    // コメントアウト中の追加セレクト
    // List<OrderHeaderDto> selectOrdersByUserId(String userId);
    // List<OrderItemDto> selectItemsByOrderIds(List<String> orderIds);
}
