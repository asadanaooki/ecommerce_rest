package com.example.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.admin.AdminDashboardDto;
import com.example.enums.DashboardRange;
import com.example.service.admin.AdminDashboardService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<AdminDashboardDto> showOverview(
            @RequestParam(defaultValue = "LAST_7_DAYS_INCLUDING_TODAY") DashboardRange range) {
        AdminDashboardDto dto = adminDashboardService.getOverview(range);

        return ResponseEntity.ok(dto);
    }
}
