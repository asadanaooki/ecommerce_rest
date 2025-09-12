package com.example.controller.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminFileDto;
import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.request.admin.AdminOrderEditRequest;
import com.example.request.admin.AdminOrderSearchRequest;
import com.example.service.OrderCommandService;
import com.example.service.admin.AdminOrderService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/admin/order")
public class AdminOrderController {
    /* TODO:
     * コントローラの分割検討
    */

    private final AdminOrderService adminOrderService;

    private final OrderCommandService orderCommandService;

    @GetMapping
    public ResponseEntity<AdminOrderListDto> search(@Valid AdminOrderSearchRequest req) {
        AdminOrderListDto dto = adminOrderService.search(req);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDetailDto> getDetail(
            @PathVariable @HexUuid @NotBlank String orderId) {
        AdminOrderDetailDto dto = adminOrderService.findDetail(orderId);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{orderId}/edit")
    public ResponseEntity<Void> edit(@PathVariable @HexUuid @NotBlank String orderId,
            @Valid @RequestBody AdminOrderEditRequest req) {
        adminOrderService.editOrder(orderId, req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/approve-cancel")
    public void approveCancel(@PathVariable @HexUuid @NotBlank String orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        orderCommandService.approveCancel(orderId, idempotencyKey);
    }

    @PostMapping("/{orderId}/ship")
    public void ship(@PathVariable @HexUuid @NotBlank String orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        orderCommandService.ship(orderId, idempotencyKey);
    }

    @PostMapping("/{orderId}/delivered")
    public void markAsDelivered(@PathVariable @HexUuid @NotBlank String orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        orderCommandService.markAsDelivered(orderId, idempotencyKey);
    }

    @GetMapping("/{orderId}/delivery-note")
    public ResponseEntity<ByteArrayResource> downloadDeliveryNote(
            @PathVariable @HexUuid @NotBlank String orderId) {
        AdminFileDto dto = adminOrderService.generateDeliveryNote(orderId);
        String encoded = URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(new ByteArrayResource(dto.getBytes()));
    }

    @GetMapping("/{orderId}/receipt")
    public ResponseEntity<ByteArrayResource> downloadReceipt(
            @PathVariable @HexUuid @NotBlank String orderId) {
        AdminFileDto dto = adminOrderService.generateReceipt(orderId);
        String encoded = URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(new ByteArrayResource(dto.getBytes()));
    }

    @GetMapping("/monthly-sales")
    public ResponseEntity<ByteArrayResource> downloadMonthlySales(
            @RequestParam @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$") String period) {
        AdminFileDto dto = adminOrderService.generateMonthlySales(period);
        String encoded = URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8);
        MediaType csv = MediaType.parseMediaType("text/csv; charset=utf-8");

        return ResponseEntity.ok()
                .contentType(csv)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(new ByteArrayResource(dto.getBytes()));
    }

}
