package com.example.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.OrderHistoryDto;
import com.example.service.OrderHistoryService;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/order-history")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping
    public List<OrderHistoryDto> showOrderHistory(@AuthenticationPrincipal String userId) {
        return orderHistoryService.findOrderHistories(userId);
    }

}
