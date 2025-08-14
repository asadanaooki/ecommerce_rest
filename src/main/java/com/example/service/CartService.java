package com.example.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;
import com.example.util.CookieUtil;
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

    private final CookieUtil cookieUtil;

    private final TaxCalculator calculator;

    public void addToCart(
            HttpServletRequest req,
            HttpServletResponse res,
            String userId,
            AddCartRequest body) {
        String cartId;
        if (userId == null) {
            cartId = cookieUtil.ensureCartId(req, res);
        } else {
            cartId = findOrCreateUserCart(userId);
        }
        cartMapper.insertCartIfAbsent(cartId, userId);
        int price = productMapper.selectByPrimaryKey(body.getProductId()).getPrice();
        cartMapper.upsertCartItem(cartId, body, price);
    }

    public void mergeGuestToUser(String guestCartId, String userCartId) {
        cartMapper.mergeCart(guestCartId, userCartId);
        cartMapper.deleteCart(guestCartId);
    }

    public CartDto showCart(int page, String userId, HttpServletRequest req) {
        String cartId = findCartId(req, userId);
        if (cartId == null) {
            return new CartDto();
        }
        List<CartItemDto> items = cartMapper.selectCartItems(cartId);
        return buildCart(items, calculator);
    }

    public String findOrCreateUserCart(String userId) {
        String candidate = UUID.randomUUID().toString();
        Map<String, String> p = new HashMap();
        p.put("userId", userId);
        p.put("candidateCartId", candidate);

        cartMapper.findOrCreateCartIdByUser(p);

        return p.get("cartId");
    }

    @Transactional
    public CartDto removeItem(String productId, String userId, HttpServletRequest req) {
        String cartId = findCartId(req, userId);
        if (cartId == null) {
            return new CartDto();
        }
        cartMapper.deleteCartItem(cartId, productId);
        List<CartItemDto> items = cartMapper.selectCartItems(cartId);
        return buildCart(items, calculator);
    }

    @Transactional
    public CartDto changeQty(String productId, int qty,
            String userId, HttpServletRequest httpReq) {
        String cartId = findCartId(httpReq, userId);
        if (cartId == null) {
            return new CartDto();
        }
        cartMapper.updateCartItemQty(cartId, productId, qty);

        List<CartItemDto> items = cartMapper.selectCartItems(cartId);
        return buildCart(items, calculator);
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

    private String findCartId(HttpServletRequest req, String userId) {
        String cartId;
        if (userId == null) {
            cartId = cookieUtil.extractCartId(req).orElse(null);
        } else {
            Cart c = cartMapper.selectCartByUser(userId);
            cartId = c == null ? null : c.getCartId();
        }
        return cartId;
    }
}
