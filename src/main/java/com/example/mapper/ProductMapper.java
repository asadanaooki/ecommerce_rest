package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.ProductCardDto;
import com.example.dto.ProductDetailDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.enums.SortType;

@Mapper
public interface ProductMapper {
    // TODO:
    // searchProductsで外部結合とサブクエリの速度比較
    // searchProductsをAND検索にする
    // クエリでstatus = '1'を良い書き方にしたい

    Product selectByPrimaryKey(String productId);
    
    ProductCoreView selectViewByPrimaryKey(String productId);

    List<ProductCardDto> searchProducts(SearchCondition sc);

    int countProducts(List<String> keywords);

    ProductDetailDto findProductDetail(String productId, String userId);

    int decreaseStock(String productId, int qty);

    record SearchCondition(String userId,
            List<String> keywords,
            SortType sort,
            int size,
            int offset) {
    }
}
