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
import com.example.entity.Product;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;
import com.example.util.TaxCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    /* TODO:
      ・トランザクションのテストは結合テストにする
      　フロントに持たせないほうが良いということで、カートIDを毎回バックで解決してる。パフォーマンス要検証
      ・削除のたびにpage=1を取得してる。毎回ジャンプするため同じ位置にした方が良いかも？
      ・販売ステータス定数を共通化したほうが良いかも。
      ・価格計算、重複して書いてるが共通化したほうが良いのか
      ・buildCartメソッドstaticで定義しているが、ちゃんと共通化したほうがよいかも
    */
    private final CartMapper cartMapper;

    private final ProductMapper productMapper;

    private final TaxCalculator calculator;

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
        List<CartItemDto> items = cartMapper.selectCartItems(cartId);
        return buildCart(items, calculator);
    }

    // TODO:
    // 引数おおい。リファクタリングする？
    /** 追加（idempotent） */
    public void addToCart(
            String cartId,
            String userId,
            String productId,
            AddCartRequest body) {
        cartMapper.insertCartIfAbsent(cartId, userId);
        int price = Optional.ofNullable(productMapper.selectByPrimaryKey(productId))
                .map(Product::getPrice)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cartMapper.upsertCartItem(new CartItem() {
            {
                setCartId(cartId);
                setProductId(productId);
                setQty(body.getQty());
                setPrice(price);
            }
        });
    }

    @Transactional
    public void removeItem(String cartId, String productId) {
        cartMapper.deleteCartItem(cartId, productId);
    }

    @Transactional
    public void changeQty(String cartId, String productId, int qty) {
        cartMapper.updateCartItemQty(cartId, productId, qty);
    }

    public void mergeGuestToUser(String guestCartId, String userCartId) {
        cartMapper.mergeCart(guestCartId, userCartId);
        cartMapper.deleteCart(guestCartId);
    }

    
    public static CartDto buildCart(List<CartItemDto> items, TaxCalculator calculator) {
        int totalQty = 0;
        int totalPrice = 0;
        for (CartItemDto it : items) {
            int priceInc = calculator.calculatePriceIncludingTax(it.getPriceEx());
            int subtotal = priceInc * it.getQty();
            it.setPriceInc(priceInc);
            it.setSubtotal(subtotal);
            totalQty += it.getQty();
            totalPrice += subtotal;
        }
        return new CartDto(items, totalQty, totalPrice);
    }
}
