package com.example.feature.product;

import org.springframework.stereotype.Component;

import com.example.entity.Product;
import com.example.mapper.ProductMapper;
import com.example.util.GuardUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductGuard {

    private final ProductMapper productMapper;
    
    
    public Product require(String productId) {
        return GuardUtil.ensureFound(productMapper.selectByPrimaryKey(productId), "PRODUCT_NOT_FOUND");
    }
}
