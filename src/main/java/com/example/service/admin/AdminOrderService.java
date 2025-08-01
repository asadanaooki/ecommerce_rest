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
import com.example.dto.admin.AdminOrderDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;
import com.example.mapper.UserMapper;
import com.example.mapper.admin.AdminOrderMapper;
import com.example.request.admin.OrderEditRequest;
import com.example.request.admin.OrderSearchRequest;
import com.example.support.MailGateway;
import com.example.util.PaginationUtil;

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
    // 税抜きで価格表示

    private final AdminOrderMapper adminOrderMapper;

    private final UserMapper userMapper;

    @Value("${settings.admin.order.size}")
    private int pageSize;

    private final MailGateway mailGateway;

    public AdminOrderListDto search(OrderSearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);

        List<AdminOrderDto> content = adminOrderMapper.selectPage(req, pageSize, offset);
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

    public void changeShippingStatus(String orderId, ShippingStatus status) {
        adminOrderMapper.updateShippingStatus(orderId, status);
    }

    public void changePaymentStatus(String orderId, PaymentStatus status) {
        adminOrderMapper.updatePaymentStatus(orderId, status);
    }

    @Transactional
    public void editOrder(String orderId, OrderEditRequest req) {
        // 準備
        Order o = adminOrderMapper.selectOrderForUpdate(orderId);
        EditContext ctx = prepareContext(o, req);

        // 処理
        processQtyReduction(ctx);
        processDeletion(ctx);
        adminOrderMapper.updateTotals(orderId);

        // メール送信
        List<OrderItem> items = adminOrderMapper.selectOrderItemsForUpdate(orderId);
        User u = userMapper.selectUserByPrimaryKey(o.getUserId());
        mailGateway.send(MailTemplate.ORDER_EDIT_COMPLETED.build(u, o.getOrderNumber(), items));
    }

    private EditContext prepareContext(Order order, OrderEditRequest req) {

        if (!(order.getPaymentStatus() == PaymentStatus.UNPAID
                && order.getShippingStatus() == ShippingStatus.NOT_SHIPPED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "STATUS_NOT_EDITABLE");
        }

        Map<String, OrderItem> oldm = adminOrderMapper.selectOrderItemsForUpdate(order.getOrderId()).stream()
                .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

        Map<String, Integer> newm = req.getItems();
        List<String> deleteIds = req.getDeleted();

        adminOrderMapper.selectProductsForUpdate(
                Stream.concat(newm.keySet().stream(), deleteIds.stream()).toList());

        return new EditContext(order.getOrderId(), oldm, newm, deleteIds);
    }

    private void processQtyReduction(EditContext ctx) {
        for (Entry<String, Integer> e : ctx.newm().entrySet()) {
            String productId = e.getKey();
            int newQty = e.getValue();
            OrderItem old = ctx.oldm.get(productId);

            if (newQty < old.getQty()) {
                adminOrderMapper.addStock(productId, old.getQty() - newQty);
                old.setQty(newQty);
                old.setSubtotal(old.getPrice() * newQty);
                adminOrderMapper.updateItemQty(old);

            } else if (newQty > old.getQty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QUANTITY_INCREASE_NOT_ALLOWED");
            }
        }
    }

    private void processDeletion(EditContext ctx) {
        for (String pid : ctx.deleteIds()) {
            OrderItem old = ctx.oldm().get(pid);

            adminOrderMapper.addStock(pid, old.getQty());
            adminOrderMapper.deleteOrderItem(ctx.orderId(), pid);
        }
    }

    private record EditContext(
            String orderId,
            Map<String, OrderItem> oldm,
            Map<String, Integer> newm,
            List<String> deleteIds) {
    }
}
