package com.example.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.order.OrderEvent;
import com.example.enums.order.OrderStatus;
import com.example.feature.order.OrderState;
import com.example.feature.order.OrderTransitionGuard;
import com.example.mapper.OrderMapper;
import com.example.mapper.UserMapper;
import com.example.support.MailGateway;
import com.example.util.OrderUtil;
import com.example.util.UserUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCommandService {
    /* TODO:
     * 管理者アドレスは仮値
     * 不在や、受け取り拒否、返送、代引き失敗といった例外ケースは保留
     * 在庫戻しの二重処理を防ぐための対策
     * キャンセル理由、時刻など持つべきか検討
     * 配達や決済ステータスは外部APIで取得？
     * 冪等性対策
        回線不安定で「もう一度送信」したときに、2回目がエラー（409）で返ると「失敗した？」 にどう対応するか？
     * キャンセル拒否機能追加検討
     * requestCancel→ユーザーに受付メール送るか検討
     * 現状orderIdは妥当なモノである前提でNPE対策してない
     * ユーザーのEmail取得する際、毎回汎用クエリで取得してる。Orderと同時に取得するカスタムクエリ作るべきか？
     * NPE対策
     */

    // private final String ADMIN_EMAIL = "admin@example.com";

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final OrderTransitionGuard guard;

    private final MailGateway gateway;

    @Transactional
    public void requestCancel(String orderId) {
        apply(orderId, OrderEvent.REQUEST_CANCEL);
    }

    @Transactional
    public void approveCancel(String orderId) {
        apply(orderId, OrderEvent.APPROVE_CANCEL);

        orderMapper.restoreInventory(orderId);

        Order o = orderMapper.selectOrderByPrimaryKey(orderId);
        List<OrderItem> items = orderMapper.selectOrderItems(orderId);
        User u = userMapper.selectUserByPrimaryKey(o.getUserId());

        gateway.send(MailTemplate.CANCEL_APPROVED
                .build(new MailTemplate.CancelApprovedContext(
                        u.getEmail(),
                        UserUtil.buildFullName(u),
                        OrderUtil.formatOrderNumber(o.getOrderNumber()),
                        items,
                        o.getItemsSubtotalIncl(),
                        o.getShippingFeeIncl(),
                        o.getCodFeeIncl(),
                        o.getGrandTotalIncl())));
    }

    @Transactional
    public void ship(String orderId) {
        OrderState cur = apply(orderId, OrderEvent.SHIP);
        orderMapper.updateShippedAt(orderId);

        Order o = orderMapper.selectOrderByPrimaryKey(orderId);
        List<OrderItem> items = orderMapper.selectOrderItems(orderId);
        User u = userMapper.selectUserByPrimaryKey(o.getUserId());

        if (cur.getOrder() == OrderStatus.OPEN) {
            gateway.send(MailTemplate.SHIPPING_NOTIFICATION.build(
                    new MailTemplate.ShipmentContext(
                            u.getEmail(),
                            o.getName(),
                            o.getAddress(),
                            OrderUtil.formatOrderNumber(o.getOrderNumber()),
                            items,
                            o.getItemsSubtotalIncl(),
                            o.getShippingFeeIncl(),
                            o.getCodFeeIncl(),
                            o.getGrandTotalIncl())));
        } else if (cur.getOrder() == OrderStatus.CANCEL_REQUESTED) {
            gateway.send(MailTemplate.SHIPPED_AND_CANCEL_REJECTED.build(
                    new MailTemplate.CancelRejectedContext(
                            u.getEmail(),
                            o.getName(),
                            OrderUtil.formatOrderNumber(o.getOrderNumber()),
                            items,
                            o.getItemsSubtotalIncl(),
                            o.getShippingFeeIncl(),
                            o.getCodFeeIncl(),
                            o.getGrandTotalIncl())));
        }
    }

    @Transactional
    public void markAsDelivered(String orderId) {
        apply(orderId, OrderEvent.DELIVERED);
    }

    private OrderState apply(String orderId, OrderEvent ev) {
        Order before = orderMapper.selectOrderByPrimaryKey(orderId);
        OrderState cur = new OrderState(
                before.getOrderStatus(),
                before.getShippingStatus(),
                before.getPaymentStatus());

        final OrderState next;
        try {
            next = guard.next(cur, ev);
        } catch (IllegalStateException e) {
            // ガード違反 → 409 + reason を返す
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_STATE_INVALID");
        }

        int updated = orderMapper.applyTransition(orderId, cur, next);
        if (updated <= 0) {
            // 楽観ロック競合など：更新されず → 409
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_STATE_CONFLICT");
        }
        return cur;
    }
}
