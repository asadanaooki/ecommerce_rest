package com.example.request.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import com.example.enums.SaleStatus;
import com.example.enums.SortFIeld;
import com.example.enums.SortOrder;

import lombok.Data;

@Data
public class ProductSearchRequest {
    // TODO:
    // qの最大文字数定義したほうがよいかも？長い文字列でどれくらいパフォーマンス影響するか検証する
    // 現状日付のフィルターだが、時刻のフィルタリングまで必要か検討
    // 日付のSetterでNULLチェックしてない、管理画面で変な操作しないだろうという前提
    // Enumのパラメータはパースできなければ、フォールバックしたい。現状エラーになる
    // 未設定項目を抽出できるようにする。現状、未設定はフィルター外

    private String q;
    
    @Min(1)
    private Integer minPrice;
    
    private Integer maxPrice;
    
    @Min(0)
    private Integer minStock;
    
    private Integer maxStock;
    
    private LocalDateTime createdFrom;
    
    private LocalDateTime createdTo;
    
    private LocalDateTime updatedFrom;
    
    private LocalDateTime updatedTo;
    
    private SaleStatus status;
    
    private SortFIeld sortFIeld = SortFIeld.UPDATED_AT;
    
    private SortOrder sortOrder = SortOrder.DESC;
    
    @Min(1)
    private int page = 1;
    
    
    @AssertTrue
    public boolean isValidPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice <= maxPrice;
    }
    
    @AssertTrue
    public boolean isValidStockRange() {
        if (minStock == null || maxStock == null) {
            return true;
        }
        return minStock <= maxStock;
    }
    
    @AssertTrue
    public boolean isValidCreatedRange() {
        if (createdFrom == null || createdTo == null) {
            return true;
        }
        // 同日含めるため、以下のような書き方してる
        return !createdFrom.isAfter(createdTo);
    }
    
    @AssertTrue
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
