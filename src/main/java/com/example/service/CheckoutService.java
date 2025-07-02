package com.example.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.dto.CheckoutDto;
import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.exception.BusinessException;
import com.example.mapper.CartMapper;
import com.example.mapper.CheckoutMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.support.MailGateway;
import com.example.util.TaxCalculator;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CheckoutService {
    /* TODO:
    ・販売ステータス定数を共通化したほうが良いかも。
    ・価格計算、重複して書いてるが共通化したほうが良いのか？
    ・将来的に手動削除も検討。
    ・メールを Tx 内で送る長時間ロック。メールの誤送信など、購入とメール送信の整合性とるには？
    ・メールに商品画像ものせる。
    ・sendCheckoutCompleteMailの引数多い、今後どうする？
    ・支払方法増やす。希望日時や送り先変更も。
    ・diffリストを別に返す方が良いかも
    */

    private final UserMapper userMapper;

    private final CartMapper cartMapper;

    private final CheckoutMapper checkoutMapper;

    private final ProductMapper productMapper;

    private final TaxCalculator calculator;

    private final MailGateway mailGateway;

    public CheckoutDto loadCheckout(String userId) {
        String cartId = cartMapper.selectCartByUser(userId).getCartId();
        Cart c = cartMapper.selectCartByPrimaryKey(cartId);
        if (c == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND);
        }
        // ユーザー情報
        User user = userMapper.selectUserByPrimaryKey(userId);
        NameAddress na = buildNameAddress(user);

        List<CartItemDto> items = cartMapper.selectCartItems(cartId);

        return new CheckoutDto(na.fullName,
                user.getPostalCode(),
                na.fullAddress,
                CartService.buildCart(items, calculator),
                c.getVersion());
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
        //            checkoutMapper.deleteRemovedItems(cartId, removeIds);
        //        }

    }

    @Transactional
    public void checkout(String userId, int version) throws MessagingException {
        Cart c = cartMapper.selectCartByUser(userId);
        // カートが更新されてる場合
        if (c.getVersion() != version) {
            throw new BusinessException(HttpStatus.CONFLICT, "cartVersion");
        }
        String orderId = c.getCartId();
        List<CartItemDto> items = checkoutMapper.selectCheckoutItems(orderId);

        // 要確認商品がある場合
        if (hasDiff(items)) {
            throw new BusinessException(HttpStatus.CONFLICT, "diff", items);
        }

        CartDto cart = CartService.buildCart(items, calculator);
        User user = userMapper.selectUserByPrimaryKey(userId);
        // 注文確定処理
        finalizeOrder(orderId, user, cart);

        // TODO:メール送信 仮実装
        mailGateway.send(MailTemplate.ORDER_CONFIRMATION.build(user, cart, orderId));
    }

    /**
     * 差分を検知し、各 Item に Reason を付与。
     * 差分が 1 件でもあれば true を返す。
     */
    private boolean hasDiff(List<CartItemDto> items) {
        for (CartItemDto it : items) {
            if (it.getStatus().equals("0")) {
                it.setReason(CartItemDto.DiffReason.DISCONTINUED);
            } else if (it.getStock() == null || it.getStock() <= 0) {
                it.setReason(CartItemDto.DiffReason.OUT_OF_STOCK);
            } else if (it.getStock() < it.getQty()) {
                it.setReason(CartItemDto.DiffReason.LOW_STOCK);
            } else if (it.getPriceEx() != it.getPriceAtCartAddition()) {
                it.setReason(CartItemDto.DiffReason.PRICE_CHANGED);
            }
        }
        return items.stream().anyMatch(i -> i.getReason() != null);
    }

    private void finalizeOrder(String orderId, User user, CartDto cart) {
        NameAddress na = buildNameAddress(user);

        /* ---------- 在庫減算 ---------- */
        cart.getItems().forEach(i -> productMapper.decreaseStock(i.getProductId(), i.getQty()));

        /* ---------- 注文ヘッダ ---------- */
        checkoutMapper.insertOrderHeader(new Order() {
            {
                setOrderId(orderId);
                setUserId(user.getUserId()); // ← ユーザ ID は user から
                setName(na.fullName);
                setPostalCode(user.getPostalCode());
                setAddress(na.fullAddress);
                setTotalQty(cart.getTotalQty());
                setTotalPrice(cart.getTotalPrice());
            }
        });

        /* ---------- 注文明細 ---------- */
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(i -> {
                    OrderItem oi = new OrderItem();
                    oi.setOrderId(orderId);
                    oi.setProductId(i.getProductId());
                    oi.setProductName(i.getProductName());
                    oi.setQty(i.getQty());
                    oi.setPrice(i.getPriceInc());
                    oi.setSubtotal(i.getSubtotal());
                    return oi;
                })
                .collect(Collectors.toList());

        checkoutMapper.insertOrderItems(orderItems);

        // カート削除
        cartMapper.deleteCart(orderId);
    }

    public static NameAddress buildNameAddress(User user) {
        String name = user.getLastName() + " " + user.getFirstName();
        String address = String.join("",
                user.getAddressPrefCity(),
                user.getAddressArea(),
                user.getAddressBlock(),
                Objects.toString(user.getAddressBuilding(), ""));
        return new NameAddress(name, address);
    }

    public record NameAddress(String fullName, String fullAddress) {
    }

}
