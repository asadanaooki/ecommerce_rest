package com.example.service.admin;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.dto.admin.AdminOrderRowDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.OrderEditCompletedContext;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.admin.AdminOrderMapper;
import com.example.request.admin.OrderEditRequest;
import com.example.request.admin.OrderSearchRequest;
import com.example.support.MailGateway;
import com.example.util.OrderUtil;
import com.example.util.PaginationUtil;
import com.example.util.UserUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    // TODO:
    // 伝票作成でのPDF出力
    // CSV出力
    // 検索の便利さのために、カナも表示したほうがよいかも
    // OrderDetailDtoを戻り値にすると、一時的にitemsがnullになる。未完全Dtoは防ぐ？
    // 税込み価格の変換忘れてしまう、早めに対策する
    // ステータスの状態遷移ルールや定義。手動から自動遷移にしたい
    // Webhook or ポーリング
    // サーバー側の防御策としての SQL ガード(現在のステータスと違うときだけ更新)入れるか
    // 現状、顧客に請求している金額（＝税込み） をベースに表示、管理や集計のための金額を考慮する場合は税込みも検討

    private final AdminOrderMapper adminOrderMapper;

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final ProductMapper productMapper;

    @Value("${settings.admin.order.size}")
    private int pageSize;

    private final MailGateway mailGateway;

    public AdminOrderListDto search(OrderSearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);

        List<AdminOrderRowDto> content = adminOrderMapper.selectPage(req, pageSize, offset);
        int total = adminOrderMapper.count(req);

        return new AdminOrderListDto(content, total, pageSize);
    }

    public AdminOrderDetailDto findDetail(String orderId) {
        // 既にDBには税込み価格が入ってるため、変換不要
        // checkout時、税込みで保存してるため
        AdminOrderDetailDto dto = adminOrderMapper.selectOrderHeader(orderId);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<AdminOrderDetailItemDto> items = adminOrderMapper.selectOrderItems(orderId);
        dto.setItems(items);

        return dto;
    }

    @Transactional
    public void editOrder(String orderId, OrderEditRequest req) {
        // 準備
        Order o = orderMapper.selectOrderByPrimaryKey(orderId);
        EditContext ctx = prepareContext(o, req);

        // 処理
        processQtyReduction(ctx);
        processDeletion(ctx);
        
        List<OrderItem> items = orderMapper.selectOrderItems(orderId);
        int itemsSubtotalIncl = OrderUtil.sumBy(items, OrderItem::getSubtotalIncl);
        orderMapper.updateTotals(
                orderId,
                itemsSubtotalIncl,
                        OrderUtil.calculateShippingFeeIncl(itemsSubtotalIncl));

        // メール送信
        List<OrderItem> items = orderMapper.selectOrderItems(orderId);
        User user = userMapper.selectUserByPrimaryKey(o.getUserId());

        mailGateway.send(MailTemplate.ORDER_EDIT_COMPLETED.build(
                new OrderEditCompletedContext(
                        user.getEmail(),
                        UserUtil.buildFullName(user),
                        UserUtil.buildFullAddress(user),
                        o.getOrderNumber(),
                        items,
                        OrderUtil.calculateGrandTotalIncl(items, null)
                        items.stream().mapToInt(OrderItem::getSubtotalIncl).sum())));
    }

    private EditContext prepareContext(Order order, OrderEditRequest req) {

        if (!(order.getPaymentStatus() == PaymentStatus.UNPAID
                && order.getShippingStatus() == ShippingStatus.UNSHIPPED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "STATUS_NOT_EDITABLE");
        }

        Map<String, OrderItem> oldm = orderMapper.selectOrderItems(order.getOrderId()).stream()
                .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

        Map<String, Integer> newm = req.getItems();
        List<String> deleteIds = req.getDeleted();

        return new EditContext(order.getOrderId(), oldm, newm, deleteIds);
    }

    private void processQtyReduction(EditContext ctx) {
        // TODO:
        // newQty = old.getQtyのときも例外出す

        for (Entry<String, Integer> e : ctx.newm().entrySet()) {
            String productId = e.getKey();
            int newQty = e.getValue();
            OrderItem old = ctx.oldm.get(productId);

            if (newQty > old.getQty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QUANTITY_INCREASE_NOT_ALLOWED");
            }
            if (newQty == old.getQty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            productMapper.increaseStock(productId, old.getQty() - newQty, null);
            old.setQty(newQty);
            orderMapper.updateItemQty(old);
        }
    }

    private void processDeletion(EditContext ctx) {
        for (String pid : ctx.deleteIds()) {
            OrderItem old = ctx.oldm().get(pid);

            productMapper.increaseStock(pid, old.getQty(), null);
            orderMapper.deleteOrderItem(ctx.orderId(), pid);
        }
    }

    private record EditContext(
            String orderId,
            Map<String, OrderItem> oldm,
            Map<String, Integer> newm,
            List<String> deleteIds) {
    }
}
