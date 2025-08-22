package com.example.enums;

import lombok.Getter;

@Getter
public enum ProductSortField {
    NAME("product_name"),
    PRICE("price_excl"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at"),
    STOCK("stock");
    
    private final String field;
    
    private ProductSortField(String field) {
        this.field = field;
    }
}
