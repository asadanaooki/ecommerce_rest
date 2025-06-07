package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.request.AddCartRequest;

@Mapper
public interface CartMapper {
    /*    TODO:
    ・CartとCartItemを分離する？
    */
    Cart selectCartByPrimaryKey(String cartId);
    
    CartItem selectCartItemByPrimaryKey(String cartId, String productId);
    
    int insertCartIfAbsent(String cartId);
    
    int upsertCartItem(@Param("cartId") String cartId, @Param("req") AddCartRequest req);
}
