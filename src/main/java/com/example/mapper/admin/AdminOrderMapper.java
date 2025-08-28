package com.example.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.dto.admin.AdminOrderDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.Product;
import com.example.request.admin.OrderSearchRequest;

@Mapper
public interface AdminOrderMapper {
    // TODO:
    // 検索で、注文番号は完全一致、名前は部分一致と動的にする
    // 部分一致のフルスキャンの性能確認
    // selectOrderItemsで、タイムスタンプが同一のため商品ID昇順で並び変えている。タイムスタンプでソートしたい
    // updateTotals→JOINとサブクエリ２回性能差比較
    
    int count(OrderSearchRequest req);
    
    List<AdminOrderDto> selectPage(OrderSearchRequest req, int limit, int offset);
    
    AdminOrderDetailDto selectOrderHeader(String orderId);
    
    // TODO:
    // 戻り値をEntityにして汎用的にするか検討
    List<AdminOrderDetailItemDto> selectOrderItems(String orderId);
    
    Order selectOrderForUpdate(String orderId);
    
    List<OrderItem> selectOrderItemsForUpdate(String orderId);
    
    List<Product> selectProductsForUpdate(List<String> productIds);
    
    int updateItemQty(OrderItem item);
    
    int addStock(String productId, int qty);
    
    int updateTotals(String orderId);
    
    int deleteOrderItem(String orderId, String productId);
}
