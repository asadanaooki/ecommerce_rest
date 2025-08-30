package com.example.controller;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.OrderHistoryDto;
import com.example.service.OrderCommandService;
import com.example.service.OrderHistoryService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/order-history")
public class OrderHistoryController {
    // TODO:
    // requestCancelの定義場所検討

    private final OrderHistoryService orderHistoryService;

    private final OrderCommandService orderCommandService;

    @GetMapping
    public List<OrderHistoryDto> showOrderHistory(@AuthenticationPrincipal String userId) {
        return orderHistoryService.findOrderHistories(userId);
    }

    @PostMapping("/{orderId}/cancel-request")
    public void requestCancel(@PathVariable @HexUuid @NotBlank String orderId) {
        orderCommandService.requestCancel(orderId);
    }

}
