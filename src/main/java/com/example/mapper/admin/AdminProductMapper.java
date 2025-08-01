package com.example.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.request.admin.ProductSearchRequest;

@Mapper
public interface AdminProductMapper {
    
    int countProducts(ProductSearchRequest req);
    
    List<ProductCoreView> searchProducts(ProductSearchRequest req, int limit, int offset);
    
    int insert(Product p);
    
    int update(Product p);
}
