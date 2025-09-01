package com.example.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.dto.admin.AdminOrderRowDto;
import com.example.request.admin.OrderSearchRequest;

@Mapper
public interface AdminOrderMapper {
    // TODO:
    // 検索で、注文番号は完全一致、名前は部分一致と動的にする
    // 部分一致のフルスキャンの性能確認
    // selectOrderItemsで、タイムスタンプが同一のため商品ID昇順で並び変えている。タイムスタンプでソートしたい
    // updateTotals→JOINとサブクエリ２回性能差比較
    
    int count(OrderSearchRequest req);
    
    List<AdminOrderRowDto> selectPage(OrderSearchRequest req, int limit, int offset);
    
    AdminOrderDetailDto selectOrderHeader(String orderId);
    
    // TODO:
    // リネーム検討
    List<AdminOrderDetailItemDto> selectOrderItems(String orderId);
}
