package com.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;

@Mapper
public interface CartMapper {
    /*    TODO:
    ・CartとCartItemを分離する？
    */
    Cart selectCartByPrimaryKey(String cartId);
    
    Cart selectCartByUser(String userId);
    
    CartItem selectCartItemByPrimaryKey(String cartId, String productId);
    
    int insertCartIfAbsent(String cartId, String userId);
    
    int upsertCartItem(CartItem item);
    
    int findOrCreateCartIdByUser(Map<String, String> cartKeyMap);
    
    int mergeCart(String guestCartId, String userCartId);
    
    int deleteCart(String cartId);
    
    int deleteCartItem(String cartId, String productId);
    
    List<CartItemDto> selectCartItems(String cartId);
    
    int updateCartItemQty(String cartId, String productId, int qty);
}
