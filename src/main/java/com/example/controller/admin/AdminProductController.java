package com.example.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminProductDetailDto;
import com.example.dto.admin.AdminProductListDto;
import com.example.request.admin.AdminProductSearchRequest;
import com.example.request.admin.AdminProductUpsertRequest;
import com.example.service.admin.AdminProductService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {
    /* TODO:
     * パスパラメータを小文字のスネークケースにすることを検討
    */
    

    private final AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<AdminProductListDto> searchProducts(@Valid AdminProductSearchRequest req) {
        AdminProductListDto dto = adminProductService.searchProducts(req);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductDetailDto> getDetail(@PathVariable @HexUuid @NotBlank String productId) {
        AdminProductDetailDto dto = adminProductService.findDetail(productId);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid AdminProductUpsertRequest req) {
        adminProductService.create(req);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> update(@PathVariable @HexUuid @NotBlank String productId,
            @Valid AdminProductUpsertRequest req) {
        adminProductService.update(productId, req);

        return ResponseEntity.ok().build();
    }
}
