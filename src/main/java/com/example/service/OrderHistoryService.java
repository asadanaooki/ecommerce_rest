package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dto.OrderHistoryDto;
import com.example.dto.OrderItemDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.mapper.OrderMapper;
import com.example.util.OrderUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderHistoryService {
    /* TODO:
     * キャンセル機能
    ・注文履歴で、取得時order byできない。日時カラムの値がinsert時全て同じになるから。
    　できればカートと同じ順序で出したいが。
    ・注文履歴表示をN回ループで実装してる。パフォーマンス計測。別方法検討
    ・注文履歴で、全件表示、全明細表示にしてるが、今後変えた方が良いかも
    ・SKU表示を検討
    ・N+１回避のため、JOIN ＋ MyBatis resultMapを検討
    */
    
    private final OrderMapper orderMapper;
    
    public List<OrderHistoryDto> findOrderHistories(String userId){
        List<Order> headers = orderMapper.selectOrdersByUserId(userId);
        List<OrderHistoryDto> result = new ArrayList<OrderHistoryDto>();
        
        for (Order o : headers) {
            List<OrderItem> items =  orderMapper.selectOrderItems(o.getOrderId());
            List<OrderItemDto> itemDtos = items.stream()
                    .map(it -> new OrderItemDto(
                            it.getProductId(),
                            it.getProductName(),
                            it.getUnitPriceIncl(),
                            it.getQty()
                            )).toList();
            
            result.add(new OrderHistoryDto() {
                {
                    setOrderId(o.getOrderId());
                    setOrderNumber(OrderUtil.formatOrderNumber(o.getOrderNumber()));
                    setOrderedAt(o.getCreatedAt().toLocalDate());
                    setName(o.getName());
                    setPostalCode(o.getPostalCode());
                    setAddress(o.getAddress());
                    setItems(itemDtos);
                    setItemsSubtotalIncl(o.getItemsSubtotalIncl());
                    setShippingFeeIncl(o.getShippingFeeIncl());
                    setGrandTotalIncl(o.getGrandTotalIncl());
                    setOrderStatus(o.getOrderStatus());
                }
            });
        }
        return result;
    }
}
