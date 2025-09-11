package com.example.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.feature.product.ProductGuard;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    /* TODO:
     *トランザクションのテストは結合テストにする
     *フロントに持たせないほうが良いということで、カートIDを毎回バックで解決してる。パフォーマンス要検証
     *削除のたびにpage=1を取得してる。毎回ジャンプするため同じ位置にした方が良いかも？
     *addToCart→引数おおい。リファクタリングする？
     */
    private final CartMapper cartMapper;

    private final ProductMapper productMapper;
    
    private final ProductGuard productGuard;

    public Optional<String> findUserCartId(String userId) {
        return Optional.ofNullable(cartMapper.selectCartByUser(userId))
                .map(Cart::getCartId);
    }

    public String getOrCreateCartId(String userId) {
        String candidate = UUID.randomUUID().toString();
        Map<String, String> p = new HashMap<>();
        p.put("userId", userId);
        p.put("candidateCartId", candidate);
        cartMapper.findOrCreateCartIdByUser(p);

        return p.get("cartId");
    }

    /** カート参照 */
    public CartDto showCart(String cartId) {
        if (cartMapper.isCartExpired(cartId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        List<CartItemDto> items = cartMapper.selectCartItems(cartId);
        return new CartDto(items, null);
    }

    /** 追加（idempotent） */
    public Optional<String> addToCart(
            String candidateCartId,
            String userId,
            String productId,
            AddCartRequest body) {
        boolean expiredOrAbsent = (candidateCartId == null)
                || cartMapper.isCartExpired(candidateCartId);
        String cid = expiredOrAbsent ? UUID.randomUUID().toString() : candidateCartId;

        cartMapper.insertCartIfAbsent(cid, userId);

        int unitPriceExcl = productGuard.require(productId).getPriceExcl();
        cartMapper.upsertCartItem(new CartItem() {
            {
                setCartId(cid);
                setProductId(productId);
                setQty(body.getQty());
                setUnitPriceExcl(unitPriceExcl);
            }
        });

        return expiredOrAbsent ? Optional.of(cid) : Optional.empty();
    }

    // --- PATCH 数量変更：期限切れなら新規発行してから反映 ---
    @Transactional
    public void changeQty(
            String cartId,
            String userId,
            String productId,
            int qty) {
        int unitPriceExcl = productGuard.require(productId).getPriceExcl();
        cartMapper.upsertCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId(productId);
                setQty(qty);
                setUnitPriceExcl(unitPriceExcl);
            }
        });
    }

    // --- DELETE：冪等。存在しなくても期限切れでも OK ---
    @Transactional
    public void removeItem(String cartId, String productId) {
        cartMapper.deleteCartItem(cartId, productId);
    }

    public void mergeGuestToUser(String guestCartId, String userCartId) {
        cartMapper.mergeCart(guestCartId, userCartId);
        cartMapper.deleteCart(guestCartId);
    }

}
