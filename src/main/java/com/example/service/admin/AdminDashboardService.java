package com.example.service.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.dto.admin.AdminDailyAggRow;
import com.example.dto.admin.AdminDashboardDto;
import com.example.dto.admin.AdminHourlyAggRow;
import com.example.enums.DashboardRange;
import com.example.mapper.admin.AdminDashboardMapper;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminDashboardService {
    // TODO:
    // 売上→カラム変更により、税抜きに変わるかも
    // アラート機能

    private final AdminDashboardMapper adminDashboardMapper;

    private static final DateTimeFormatter H_LABEL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

    public AdminDashboardDto getOverview(DashboardRange range) {
        switch (range) {
        case TODAY: return buildToday();
        case MONTH_TO_DATE: {
            LocalDate today = LocalDate.now();
            LocalDate start = YearMonth.from(today).atDay(1);
            LocalDate end = today.plusDays(1);
            
            return buildDaily(start, end);
        }
        case LAST_7_DAYS_INCLUDING_TODAY:{
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusDays(6);
            LocalDate end = today.plusDays(1);
            
            return buildDaily(start, end);
        }
        default:
            throw new IllegalArgumentException("Unknown range: " + range);
        }

    }

    private AdminDashboardDto buildToday() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.truncatedTo(ChronoUnit.DAYS);

        int lastHour = end.getHour();
        List<String> labels = new ArrayList<String>();
        for (int h = 0; h <= lastHour; h++) {
            labels.add(H_LABEL.format(start.plusHours(h)));
        }

        List<AdminHourlyAggRow> rows = adminDashboardMapper.aggTodayHourly(start, end);
        Map<String, AdminHourlyAggRow> byHour = rows.stream()
                .collect(Collectors.toMap(r -> r.getBucketHour(), r -> r));

        List<Integer> revenue = new ArrayList<Integer>();
        List<Integer> orders = new ArrayList<Integer>();
        List<Integer> aov = new ArrayList<Integer>();
        
        for(String label: labels) {
            AdminHourlyAggRow r = byHour.get(label);
            int rev = (r == null ? 0 : r.getRevenue());
            int ord = (r == null ? 0 : r.getOrders());
            revenue.add(rev);
            orders.add(ord);
            aov.add(ord == 0 ? null : calculateAov(rev, ord));
        }
        
        return new AdminDashboardDto(labels, revenue, orders, aov);
    }
    
    private AdminDashboardDto buildDaily(LocalDate start, LocalDate end) {
        List<AdminDailyAggRow> rows = adminDashboardMapper.aggDaily(start, end);
        Map<LocalDate, AdminDailyAggRow> byDay = rows.stream()
                .collect(Collectors.toMap(r -> r.getDay(), r -> r));
        
        List<String> labels = new ArrayList<>();
        List<Integer> revenue = new ArrayList<Integer>();
        List<Integer> orders = new ArrayList<Integer>();
        List<Integer> aov = new ArrayList<Integer>();
        
        LocalDate today = LocalDate.now();
        for (LocalDate d = start; !d.isAfter(today); d=d.plusDays(1)) {
            AdminDailyAggRow r = byDay.get(d);
            int rev = (r == null ? 0 : r.getRevenue());
            int ord = (r == null ? 0 : r.getOrders());
            labels.add(d.toString());
            revenue.add(rev);
            orders.add(ord);
            aov.add(ord == 0 ? null : calculateAov(rev, ord));
        }
        
        return new AdminDashboardDto(labels, revenue, orders, aov);
    }
    
    private Integer calculateAov(int revenue, int orders) {
        BigDecimal r = BigDecimal.valueOf(revenue);
        BigDecimal o = BigDecimal.valueOf(orders);
        return r.divide(o, 0, RoundingMode.HALF_UP).intValueExact();
    }
}
