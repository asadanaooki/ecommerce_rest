package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.CartItemDto;
import com.example.dto.OrderItemDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;

@Mapper
public interface CheckoutMapper {
    /*
     * TODO:
     * deleteRemovedItemsは購入不可商品判定を確認画面に入れるときに必要かも
     * 小規模やから、注文関連はすべてこのmapperに定義
     */

      List<CartItemDto> selectCheckoutItems(String cartId);
      
      Order selectOrderByPrimaryKey(String orderId);
      

      
      int insertOrderHeader(Order header);
      
      int insertOrderItems(List<OrderItem> items);
      
//    int deleteRemovedItems(String cartId, List<String> productIds);
      
    //  List<OrderHeaderDto> selectHeadersByUser(String userId);
      
      List<OrderItemDto> selectItemsByOrderIds(List<String> orderIds);
}
