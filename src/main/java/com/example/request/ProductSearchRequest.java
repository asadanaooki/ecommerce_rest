package com.example.request;

import java.util.List;

import com.example.bind.annotation.EnumFallback;
import com.example.enums.ProductSortOption;
import com.example.util.SearchUtil;

import lombok.Data;

@Data
public class ProductSearchRequest {

    private String q;

    @EnumFallback("NEW")
    private ProductSortOption sort = ProductSortOption.NEW;

    private int page = 1;
    
    
    public List<String> getKeywords() {
        return SearchUtil.extractKeywords(q);
    }
}
