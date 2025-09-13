package com.example.request.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import com.example.bind.annotation.EnumFallback;
import com.example.enums.OrderSortField;
import com.example.enums.SortDirection;
import com.example.enums.order.OrderStatus;
import com.example.enums.order.PaymentStatus;

import lombok.Data;

@Data
public class AdminOrderSearchRequest {
    /* TODO:
     * 複数検索ワード対応、現状１語検索
        メールアドレス（部分一致 or 前方一致）
        電話番号（ハイフン除去して部分一致）
        郵便番号（ハイフン除去して前方一致：例 150→1500001 を拾える）
        住所（スペース・全半角差を無視した部分一致）
     * 検索ワードの長さ制限検討
     * 多重ソート検討
     * getKeywordはSearchUtiを使用する
    */

    private String q;

    @EnumFallback
    private OrderStatus orderStatus;

    @EnumFallback
    private PaymentStatus paymentStatus;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;

    @EnumFallback("CREATED_AT")
    private OrderSortField sortField = OrderSortField.CREATED_AT;

    @EnumFallback("DESC")
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
        this.createdFrom = (date == null) ? null : date.atStartOfDay();
    }

    public void setCreatedTo(LocalDate date) {
        this.createdTo = (date == null) ? null : date.plusDays(1).atStartOfDay();
    }

    public String getKeyword() {
        if (q == null || q.strip().isEmpty()) {
            return null;
        }
        return q.replaceAll("[\\s\\p{Zs}]+", "");
    }
}
