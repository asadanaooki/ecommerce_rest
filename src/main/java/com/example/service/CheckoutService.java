package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.dto.CheckoutConfirmDto;
import com.example.dto.CheckoutItemDto;
import com.example.dto.CheckoutProcessDto;
import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.OrderConfirmationContext;
import com.example.enums.SaleStatus;
import com.example.error.BusinessException;
import com.example.mapper.CartMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.support.MailGateway;
import com.example.util.UserUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CheckoutService {
    /* TODO:
    ・販売ステータス定数を共通化したほうが良いかも。
    ・将来的に手動削除も検討。
    ・メールを Tx 内で送る長時間ロック。メールの誤送信など、購入とメール送信の整合性とるには？
    ・メールに商品画像ものせる。
    ・sendCheckoutCompleteMailの引数多い、今後どうする？
    ・支払方法増やす。希望日時や送り先変更も。
    ・diffリストを別に返す方が良いかも
    ・送料・手数料導入
    ・カートのバージョン一致導入する？カートの内容が変わってないことを保証する
    */

    private final UserMapper userMapper;

    private final CartMapper cartMapper;

    private final OrderMapper orderMapper;

    private final ProductMapper productMapper;

    private final MailGateway mailGateway;

    public CheckoutConfirmDto loadCheckout(String userId) {
        String cartId = cartMapper.selectCartByUser(userId).getCartId();
        Cart c = cartMapper.selectCartByPrimaryKey(cartId);
        if (c == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND);
        }
        // ユーザー情報
        User user = userMapper.selectUserByPrimaryKey(userId);

        List<CartItemDto> items = cartMapper.selectCartItems(cartId);

        return new CheckoutConfirmDto(
                UserUtil.buildFullName(user),
                user.getPostalCode(),
                UserUtil.buildFullAddress(user),
                new CartDto(items));
        // TODO:
        // 以下、購入不可判定入れるときに必要かも
        //   List<CartItemDto> items = new ArrayList<CartItemDto>();
        // List<RemovedItemDto> removed = new ArrayList<RemovedItemDto>();

        //        for (CartItemDto it : li) {
        //            // 販売停止
        //            if (STATUS_DISCONTINUED.equals(it.getStatus())) {
        //                removed.add(
        //                        new RemovedItemDto(
        //                                it.getProductId(), it.getProductName(), RemovedItemDto.Reason.DISCONTINUED));
        //                continue;
        //            }
        //            // 在庫切れ
        //            if (it.getStock() <= 0) {
        //                removed.add(new RemovedItemDto(
        //                        it.getProductId(), it.getProductName(), RemovedItemDto.Reason.OUT_OF_STOCK));
        //                continue;
        //            }
        //            // 以下、購入可能品
        //            it.setPriceInc(calculator.calculatePriceIncludingTax(it.getPriceEx()));
        //            it.setSubtotal(it.getPriceInc() * it.getQty());
        //
        //            // 在庫不足
        //            it.setLowStock(it.getStock() < it.getQty());
        //            // 価格改定
        //            it.setPriceChanged(it.getPriceEx() != it.getPriceAtCartAddition());
        //
        //            items.add(it);
        //        }
        //        int totalQty = items.stream().mapToInt(CartItemDto::getQty).sum();
        //        int totalPrice = items.stream().mapToInt(CartItemDto::getSubtotal).sum();
        //
        //        List<String> removeIds = removed.stream()
        //                .map(RemovedItemDto::getProductId)
        //                .collect(Collectors.toList());
        //        // 自動削除
        //        if (removeIds.size() > 0) {
        //            orderMapper.deleteRemovedItems(cartId, removeIds);
        //        }

    }

    @Transactional
    public void checkout(String userId) throws MessagingException {
        Cart c = cartMapper.selectCartByUser(userId);
        //        // カートが更新されてる場合
        //        if (c.getVersion() != version) {
        //            throw new BusinessException(HttpStatus.CONFLICT, "cartVersion");
        //        }
        String orderId = c.getCartId();
        List<CheckoutItemDto> items = orderMapper.selectCheckoutItems(orderId);

        // 要確認商品がある場合
        if (hasDiff(items)) {
            throw new BusinessException(HttpStatus.CONFLICT, "diff", items);
        }

        User user = userMapper.selectUserByPrimaryKey(userId);

        CheckoutProcessDto ck = new CheckoutProcessDto(
                UserUtil.buildFullName(user),
                user.getPostalCode(),
                UserUtil.buildFullAddress(user),
                items);

        // 注文確定処理
        int orderNumber = finalizeOrder(orderId, user, ck);

        // TODO:メール送信 仮実装
        // 管理者にも通知する
        mailGateway.send(MailTemplate.ORDER_CONFIRMATION.build(
                new OrderConfirmationContext(
                        user.getEmail(),
                        UserUtil.buildFullName(user),
                        UserUtil.buildFullAddress(user),
                        orderNumber,
                        ck.getItems())));
    }

    /**
     * 差分を検知し、各 Item に Reason を付与。
     * 差分が 1 件でもあれば true を返す。
     */
    private boolean hasDiff(List<CheckoutItemDto> items) {
        for (CheckoutItemDto it : items) {
            if (it.getStatus() == SaleStatus.UNPUBLISHED) {
                it.setReason(CheckoutItemDto.DiffReason.DISCONTINUED);
            } else if (it.getAvailable() <= 0) {
                it.setReason(CheckoutItemDto.DiffReason.OUT_OF_STOCK);
            } else if (it.getAvailable() < it.getQty()) {
                it.setReason(CheckoutItemDto.DiffReason.LOW_STOCK);
            } else if (it.getCurrentUnitPriceExcl() != it.getUnitPriceExclAtAddToCart()) {
                it.setReason(CheckoutItemDto.DiffReason.PRICE_CHANGED);
            }
        }
        return items.stream().anyMatch(i -> i.getReason() != null);
    }

    private int finalizeOrder(String orderId, User user, CheckoutProcessDto ck) {

        /* ---------- 在庫減算 ---------- */
        ck.getItems().forEach(i -> productMapper
                .decreaseStock(i.getProductId(), i.getQty(), null));

        /* ---------- 注文ヘッダ ---------- */
        Order order = new Order() {
            {
                setOrderId(orderId);
                setUserId(user.getUserId()); // ← ユーザ ID は user から
                setName(ck.getFullName());
                setPostalCode(user.getPostalCode());
                setAddress(ck.getFullAddress());
                setTotalQty(ck.getTotalQty());
                setItemsSubtotalIncl(ck.getItemsSubtotalIncl());
                setShippingFeeIncl(ck.getShippingFeeIncl());
            }
        };
        orderMapper.insertOrderHeader(order);

        /* ---------- 注文明細 ---------- */
        List<OrderItem> orderItems = ck.getItems().stream()
                .map(i -> {
                    OrderItem oi = new OrderItem();
                    oi.setOrderId(orderId);
                    oi.setProductId(i.getProductId());
                    oi.setProductName(i.getProductName());
                    oi.setQty(i.getQty());
                    oi.setUnitPriceIncl(i.getUnitPriceIncl());
                    return oi;
                })
                .collect(Collectors.toList());

        orderMapper.insertOrderItems(orderItems);

        // カート削除
        cartMapper.deleteCart(orderId);

        return order.getOrderNumber();
    }

}
