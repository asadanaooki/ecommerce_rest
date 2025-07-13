package com.example.enums;

import lombok.Getter;

@Getter
public enum SortFIeld {
    NAME("product_name"),
    PRICE("price"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at"),
    STOCK("stock");
    
    private final String field;
    
    private SortFIeld(String field) {
        this.field = field;
    }
}
