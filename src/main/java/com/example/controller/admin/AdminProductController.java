package com.example.controller.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminProductDetailDto;
import com.example.dto.admin.AdminProductListDto;
import com.example.request.admin.ProductSearchRequest;
import com.example.request.admin.ProductUpsertRequest;
import com.example.service.admin.AdminProductService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {
    // TODO:
    // パスパラメータを小文字のスネークケースにすることを検討

    private final AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<AdminProductListDto> searchProducts(@Valid ProductSearchRequest req) {
        AdminProductListDto dto = adminProductService.searchProducts(req);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductDetailDto> getDetail(@PathVariable String productId) {
        AdminProductDetailDto dto = adminProductService.findDetail(productId);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid ProductUpsertRequest req) {
        adminProductService.create(req);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> update(@PathVariable String productId, @Valid ProductUpsertRequest req) {
        adminProductService.update(productId, req);

        return ResponseEntity.ok().build();
    }
}
