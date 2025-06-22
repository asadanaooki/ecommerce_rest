package com.example.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CartDto;
import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;
import com.example.util.CookieUtil;
import com.example.util.PaginationUtil;
import com.example.util.TaxCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    // TODO:
    // トランザクションのテストは結合テストにする

    private final CartMapper cartMapper;

    private final ProductMapper productMapper;

    @Value("${settings.cart.size}")
    private int pageSize;

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

    public CartDto showCart(
            HttpServletRequest req,
            HttpServletResponse res,
            String userId,
            int page) {
        String cartId;
        if (userId == null) {
            cartId = cookieUtil.extractCartId(req).orElse(null);
        } else {
            Cart c = cartMapper.selectCartByUser(userId);
            cartId = c == null ? null : c.getCartId();
        }
        if (cartId == null) {
            return new CartDto();
        }
        
        int offset = PaginationUtil.calculateOffset(page, pageSize);
        List<CartItemDto> items = cartMapper.selectCartItemsPage(cartId, pageSize, offset);
        
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
        return new CartDto(cartId, items, totalQty, totalPrice);
    }

    public String findOrCreateUserCart(String userId) {
        String candidate = UUID.randomUUID().toString();
        Map<String, String> p = new HashMap();
        p.put("userId", userId);
        p.put("candidateCartId", candidate);

        cartMapper.findOrCreateCartIdByUser(p);

        return p.get("cartId");
    }


}
