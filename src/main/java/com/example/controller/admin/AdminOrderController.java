package com.example.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;
import com.example.request.admin.OrderEditRequest;
import com.example.request.admin.OrderSearchRequest;
import com.example.service.admin.AdminOrderService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/admin/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<AdminOrderListDto> search(@Valid OrderSearchRequest req) {
        AdminOrderListDto dto = adminOrderService.search(req);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDetailDto> getDetail(@PathVariable @HexUuid @NotBlank String orderId) {
        AdminOrderDetailDto dto = adminOrderService.findDetail(orderId);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{orderId}/shipping-status")
    public ResponseEntity<Void> updateShippingStatus(@PathVariable @HexUuid @NotBlank String orderId,
            @RequestBody ShippingStatus status) {
        adminOrderService.changeShippingStatus(orderId, status);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}/payment-status")
    public ResponseEntity<Void> updateShippingStatus(@PathVariable @HexUuid @NotBlank String orderId,
            @RequestBody PaymentStatus status) {
        adminOrderService.changePaymentStatus(orderId, status);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}/edit")
    public ResponseEntity<Void> edit(@PathVariable @HexUuid @NotBlank String orderId,
            @Valid @RequestBody OrderEditRequest req) {
        adminOrderService.editOrder(orderId, req);

        return ResponseEntity.ok().build();
    }

}
