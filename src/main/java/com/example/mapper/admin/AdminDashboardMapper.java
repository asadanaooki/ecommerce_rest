package com.example.mapper.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.admin.AdminDailyAggRow;
import com.example.dto.admin.AdminHourlyAggRow;

@Mapper
public interface AdminDashboardMapper {
    /* TODO:
     * payment_statusにindex貼る？
    */

    List<AdminHourlyAggRow> aggTodayHourly(LocalDateTime startInclusive, LocalDateTime endExclusive);
    
    List<AdminDailyAggRow> aggDaily(LocalDate startInclusive, LocalDate endExclusive);
}
