package com.example.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.request.InquiryRequest;
import com.example.service.InquiryService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<Void> submit(@Valid @RequestBody InquiryRequest req) {
        inquiryService.handle(req);
        
        return ResponseEntity.ok().build();
    }
    

}
