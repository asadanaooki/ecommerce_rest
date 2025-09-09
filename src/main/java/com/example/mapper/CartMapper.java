package com.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;

@Mapper
public interface CartMapper {
    /* TODO:
     *CartとCartItemを分離する？
    */
    
    
 // ========= SELECT =========
    Cart selectCartByPrimaryKey(String cartId);

    Cart selectCartByUser(String userId);

    CartItem selectCartItemByPrimaryKey(String cartId, String productId);

    List<CartItemDto> selectCartItems(String cartId);

    boolean isCartExpired(String cartId);


    // ========= INSERT =========
    int insertCartIfAbsent(String cartId, String userId);

    int findOrCreateCartIdByUser(Map<String, String> cartKeyMap);


    // ========= UPDATE =========
    int upsertCartItem(CartItem item);

    int mergeCart(String guestCartId, String userCartId);


    // ========= DELETE =========
    int deleteCartItem(String cartId, String productId);

    int deleteCart(String cartId);
    
    
    // バッチ系
    int deleteExpiredCarts();
}
