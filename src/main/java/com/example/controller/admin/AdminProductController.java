package com.example.controller.admin;

import java.io.IOException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminProductListDto;
import com.example.request.admin.ProductRegistrationRequest;
import com.example.request.admin.ProductSearchRequest;
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
    public ResponseEntity<AdminProductListDto> searchProducts(@Valid ProductSearchRequest req){
        AdminProductListDto dto = adminProductService.searchProducts(req);
        
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<Void> register(@Valid ProductRegistrationRequest req) throws IOException{
        adminProductService.create(req);
        
        return ResponseEntity.ok().build();
    }
}
