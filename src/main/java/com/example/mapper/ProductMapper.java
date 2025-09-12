package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.ProductCardDto;
import com.example.dto.ProductDetailDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.request.ProductSearchRequest;

@Mapper
public interface ProductMapper {
    /* TODO:
     * searchProductsで外部結合とサブクエリの速度比較
     * searchProductsの引数まとめるか検討
    */

    Product selectByPrimaryKey(String productId);
    
    ProductCoreView selectViewByPrimaryKey(String productId);

    List<ProductCardDto> searchProducts(String userId, ProductSearchRequest req,
            int limit, int offset);

    int countProducts(List<String> keywords);

    ProductDetailDto findProductDetail(String productId, String userId);

    int increaseStock(String productId, int qty, Integer version);
    
    int decreaseStock(String productId, int qty, Integer version);

}
