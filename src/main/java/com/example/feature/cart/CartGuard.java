package com.example.feature.cart;

import org.springframework.stereotype.Component;

import com.example.entity.Cart;
import com.example.mapper.CartMapper;
import com.example.util.GuardUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartGuard {
    
    private final CartMapper cartMapper;

    
    public Cart require(String cartId) {
        return GuardUtil.ensureFound(cartMapper.selectCartByPrimaryKey(cartId),
                "CART_NOT_FOUND");
    }
}
