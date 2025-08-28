package com.example.request.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import com.example.enums.OrderSortField;
import com.example.enums.SortDirection;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;

import lombok.Data;

@Data
public class OrderSearchRequest {
    // TODO:
    // 複数検索ワード対応、現状１語検索
    // 検索ワードの長さ制限検討
    // 多重ソート検討

    private String q;
    
    private OrderStatus orderStatus;
    
    private PaymentStatus paymentStatus;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;

    private OrderSortField sortField = OrderSortField.CREATED_AT;

    private SortDirection sortDirection = SortDirection.DESC;

    @Min(1)
    private int page = 1;

    @AssertTrue(message = "CREATED_RANGE_VALID")
    public boolean isValidCreatedRange() {
        if (createdFrom == null || createdTo == null) {
            return true;
        }
        // 同日含めるため、以下のような書き方してる
        return !createdFrom.isAfter(createdTo);
    }

    public void setCreatedFrom(LocalDate date) {
        createdFrom = date.atStartOfDay();
    }

    public void setCreatedTo(LocalDate date) {
        createdTo = date.atTime(LocalTime.MAX);
    }

    public String getKeyword() {
        if (q == null || q.strip().isEmpty()) {
            return null;
        }
        String noSpaces = q.replaceAll("[\\s\\p{Zs}]+", "");
        String norm = noSpaces.replaceFirst("^0+", "");
        return norm.isEmpty() ? "0" : norm;
    }
}
