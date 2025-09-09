package com.example.request.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import com.example.enums.ProductSortField;
import com.example.enums.SaleStatus;
import com.example.enums.SortDirection;

import lombok.Data;

@Data
public class ProductSearchRequest {
    /* TODO:
     * qの最大文字数定義したほうがよいかも？長い文字列でどれくらいパフォーマンス影響するか検証する
     * 現状日付のフィルターだが、時刻のフィルタリングまで必要か検討
     * 日付のSetterでNULLチェックしてない、管理画面で変な操作しないだろうという前提
     * Enumのパラメータはパースできなければ、フォールバックしたい。現状エラーになる。
         また、フォールバック処理を他の部分でも使用するなら共通化する？
     * 未設定項目を抽出できるようにする。現状、未設定はフィルター外
    */

    private String q;
    
    @Min(1)
    private Integer minPrice;
    
    private Integer maxPrice;
    
    @Min(0)
    private Integer minAvailable;
    
    private Integer maxAvailable;
    
    private LocalDateTime createdFrom;
    
    private LocalDateTime createdTo;
    
    private LocalDateTime updatedFrom;
    
    private LocalDateTime updatedTo;
    
    private SaleStatus status;
    
    private ProductSortField sortFIeld = ProductSortField.UPDATED_AT;
    
    private SortDirection sortDirection = SortDirection.DESC;
    
    @Min(1)
    private int page = 1;
    
    
    @AssertTrue(message = "PRICE_RANGE_VALID")
    public boolean isValidPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice <= maxPrice;
    }
    
    @AssertTrue(message = "STOCK_RANGE_VALID")
    public boolean isValidStockRange() {
        if (minAvailable == null || maxAvailable == null) {
            return true;
        }
        return minAvailable <= maxAvailable;
    }
    
    @AssertTrue(message = "CREATED_RANGE_VALID")
    public boolean isValidCreatedRange() {
        if (createdFrom == null || createdTo == null) {
            return true;
        }
        // 同日含めるため、以下のような書き方してる
        return !createdFrom.isAfter(createdTo);
    }
    
    @AssertTrue(message = "UPDATED_RANGE_VALID")
    public boolean isValidUpdatedRange() {
        if (updatedFrom == null || updatedTo == null) {
            return true;
        }
        // 同日含めるため、以下のような書き方してる
        return !updatedFrom.isAfter(updatedTo);
    }
    
    
    public void setCreatedFrom(LocalDate date) {
        createdFrom = date.atStartOfDay();
    }
    
    public void setCreatedTo(LocalDate date) {
        createdTo = date.atTime(LocalTime.MAX);
    }
    
    public void setUpdatedFrom(LocalDate date) {
        updatedFrom = date.atStartOfDay();
    }
    
    public void setUpdatedTo(LocalDate date) {
        updatedTo = date.atTime(LocalTime.MAX);
    }
    
    
    public List<String> getKeywords(){
        if (q == null || q.strip().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.stream(q.strip().split("[\\s\\p{Zs}]+"))
                .filter(s -> !s.isEmpty()).toList();
    }
}
