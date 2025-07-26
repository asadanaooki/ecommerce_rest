package com.example.request.admin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OrderEditRequest {

    @NotEmpty
    private Map<@NotBlank String,@NotNull @Min(1) Integer> items = Collections.EMPTY_MAP;

    private List<@NotBlank String> deleted = Collections.EMPTY_LIST;
    
    
    @AssertTrue
    private boolean isDisjoint() {
        return Collections.disjoint(items.keySet(), deleted);
    }
}
