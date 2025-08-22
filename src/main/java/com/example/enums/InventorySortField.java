package com.example.enums;

import lombok.Getter;

@Getter
public enum InventorySortField {
    UPDATED_AT("updated_at"),
    SKU("sku"),
    NAME("name"),
    PRICE("price_excl"),
    STATUS("stock_status"),
    AVAILABLE("available");
    
    private final String field;
    
    private InventorySortField(String field) {
        this.field = field;
    }
}
