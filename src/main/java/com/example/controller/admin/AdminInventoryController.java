package com.example.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminInventoryDetailDto;
import com.example.dto.admin.AdminInventoryListDto;
import com.example.request.admin.InventoryMovementRequest;
import com.example.request.admin.InventorySearchRequest;
import com.example.service.admin.AdminInventoryService;
import com.example.validation.constraint.HexUuid;

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
    public ResponseEntity<AdminInventoryDetailDto> find(@PathVariable @HexUuid @NotBlank String productId) {
        AdminInventoryDetailDto dto = adminInventoryService.getDetail(productId);

        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/{productId}/receipt")
    public ResponseEntity<Void> receive(@PathVariable @HexUuid @NotBlank String productId,
            @Valid @RequestBody InventoryMovementRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        adminInventoryService.receiveStock(productId, req, idempotencyKey);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{productId}/issue")
    public ResponseEntity<Void> issue(@PathVariable @HexUuid @NotBlank String productId,
            @Valid @RequestBody InventoryMovementRequest req,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        adminInventoryService.issueStock(productId, req, idempotencyKey);
        
        return ResponseEntity.ok().build();
    }

}
