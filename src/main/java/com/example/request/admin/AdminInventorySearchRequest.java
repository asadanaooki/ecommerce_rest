package com.example.request.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import com.example.bind.annotation.EnumFallback;
import com.example.enums.InventorySortField;
import com.example.enums.SortDirection;
import com.example.enums.StockStatus;

import lombok.Data;

@Data
public class AdminInventorySearchRequest {

    @Min(0)
    private Integer minAvailable;
    
    private Integer maxAvailable;

    @EnumFallback
    private StockStatus stockStatus;

    @EnumFallback("UPDATED_AT")
    private InventorySortField sortField = InventorySortField.UPDATED_AT;

    @EnumFallback("DESC")
    private SortDirection sortDirection = SortDirection.DESC;

    @Min(1)
    private int page = 1;

    @AssertTrue(message = "AVAILABLE_RANGE_VALID")
    public boolean isValidAvailableRange() {
        if (minAvailable == null || maxAvailable == null) {
            return true;
        }
        return minAvailable <= maxAvailable;
    }

}
