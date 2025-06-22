package com.example.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mapper.CartMapper;
import com.example.mapper.ProductMapper;
import com.example.request.AddCartRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class CartService {
    // TODO:
    // トランザクションのテストは結合テストにする
    
    private final CartMapper cartMapper;
    
    private final ProductMapper productMapper;

    public void addToCart(String cartId, String userId, AddCartRequest req) {
        cartMapper.insertCartIfAbsent(cartId, userId);
        int price = productMapper.selectByPrimaryKey(req.getProductId()).getPrice();
        cartMapper.upsertCartItem(cartId, req, price);
    }
    
    public String findOrCreateUserCart(String userId) {
        String candidate = UUID.randomUUID().toString();
        Map<String, String> p = new HashMap();
        p.put("userId", userId);
        p.put("candidateCartId", candidate);
        
        cartMapper.findOrCreateCartIdByUser(p);
        
        return p.get("cartId");
    }
    
    public void mergeGuestToUser(String guestCartId, String userCartId) {
        cartMapper.mergeCart(guestCartId, userCartId);
        cartMapper.deleteCart(guestCartId);
    }
}
