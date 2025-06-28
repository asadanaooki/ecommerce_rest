package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.entity.Order;
import com.example.entity.OrderItem;

@Mapper
public interface OrderHistoryMapper {

    List<Order> selectHeadersByUser(String userId);
    
    List<OrderItem> selectOrderItems(String orderId);

    //  List<OrderItemDto> selectItemsByOrderIds(List<String> orderIds);
}
