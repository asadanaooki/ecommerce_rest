package com.example.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.request.admin.OrderEditRequest;
import com.example.request.admin.OrderSearchRequest;
import com.example.service.OrderCommandService;
import com.example.service.admin.AdminOrderService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;


@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/admin/order")
public class AdminOrderController {
    // TODO:
    // コントローラの分割検討

    private final AdminOrderService adminOrderService;
    
    private final OrderCommandService orderCommandService;

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

    @PatchMapping("/{orderId}/edit")
    public ResponseEntity<Void> edit(@PathVariable @HexUuid @NotBlank String orderId,
            @Valid @RequestBody OrderEditRequest req) {
        adminOrderService.editOrder(orderId, req);

        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/approve-cancel")
    public void approveCancel(@PathVariable @HexUuid String orderId) {
        orderCommandService.approveCancel(orderId);
    }
    
    @PostMapping("/{orderId}/ship")
    public void ship(@PathVariable @HexUuid String orderId) {
        orderCommandService.ship(orderId);
    }
    
    @PostMapping("/{orderId}/delivered")
    public void markAsDelivered(@PathVariable @HexUuid String orderId) {
        orderCommandService.markAsDelivered(orderId);
    }

}
