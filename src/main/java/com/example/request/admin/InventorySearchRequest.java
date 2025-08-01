package com.example.request.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import com.example.enums.InventorySortField;
import com.example.enums.SortDirection;
import com.example.enums.StockStatus;

import lombok.Data;

@Data
public class InventorySearchRequest {

    @Min(0)
    private Integer minAvailable;
    
    private Integer maxAvailable;

    private StockStatus stockStatus;

    private InventorySortField sortField = InventorySortField.UPDATED_AT;

    private SortDirection sortDirection = SortDirection.DESC;

    @Min(1)
    private int page = 1;

    @AssertTrue
    public boolean isValidAvailableRange() {
        if (minAvailable == null || maxAvailable == null) {
            return true;
        }
        return minAvailable <= maxAvailable;
    }

}
