package com.example.controller.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminInventoryDetailDto;
import com.example.dto.admin.AdminInventoryListDto;
import com.example.request.admin.InventoryAdjustRequest;
import com.example.request.admin.InventorySearchRequest;
import com.example.service.admin.AdminInventoryService;

import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final AdminInventoryService adminInventoryService;

    @GetMapping
    public ResponseEntity<AdminInventoryListDto> search(@Valid InventorySearchRequest req) {
        AdminInventoryListDto dto = adminInventoryService.search(req);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminInventoryDetailDto> find(@PathVariable String productId) {
        AdminInventoryDetailDto dto = adminInventoryService.getDetail(productId);

        return ResponseEntity.ok(dto);
    }
    
    @PutMapping("/{productId}/levels")
    public ResponseEntity<Void> adjust(@PathVariable String productId, @Valid @RequestBody InventoryAdjustRequest req) {
        adminInventoryService.adjust(productId, req);
        
        return ResponseEntity.ok().build();
    }

}
