package com.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.CartItemDto;
import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.request.AddCartRequest;

@Mapper
public interface CartMapper {
    /*    TODO:
    ・CartとCartItemを分離する？
    */
    Cart selectCartByPrimaryKey(String cartId);
    
    Cart selectCartByUser(String userId);
    
    CartItem selectCartItemByPrimaryKey(String cartId, String productId);
    
    int insertCartIfAbsent(String cartId, String userId);
    
   // int upsertCartItem(@Param("cartId") String cartId, @Param("req") AddCartRequest req, int price);
    int upsertCartItem(String cartId, AddCartRequest req, int price);
    
    int findOrCreateCartIdByUser(Map<String, String> cartKeyMap);
    
    int mergeCart(String guestCartId, String userCartId);
    
    int deleteCart(String cartId);
    
    List<CartItemDto> selectCartItemsPage(String cartId, int limit, int offset);
}
