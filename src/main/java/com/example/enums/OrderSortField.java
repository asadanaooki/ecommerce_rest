package com.example.enums;

import lombok.Getter;

@Getter
public enum OrderSortField {
    /* TODO:
     * ステータスのソート必要な場合は、順序を定義する。Enum文字列で保存しているため。
    */
    
    ORDER_NUMBER("order_number"),
    NAME("name"),
    CREATED_AT("created_at");
    
    private final String field;
    
    private OrderSortField(String field) {
        this.field = field;
    }
}
