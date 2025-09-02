package com.example.service.admin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.admin.AdminDailyAggRow;
import com.example.dto.admin.AdminDashboardDto;
import com.example.dto.admin.AdminHourlyAggRow;
import com.example.enums.DashboardRange;
import com.example.mapper.admin.AdminDashboardMapper;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @InjectMocks
    AdminDashboardService adminDashboardService;

    @Mock
    AdminDashboardMapper adminDashboardMapper;

    @Nested
    class GetOverview {

        @Test
        void getOverview_today() {
            AdminHourlyAggRow r1 = new AdminHourlyAggRow();
            r1.setBucketHour("2025-08-06 00:00:00");
            r1.setRevenue(1000);
            r1.setOrders(1);
            
            AdminHourlyAggRow r2 = new AdminHourlyAggRow();
            r2.setBucketHour("2025-08-06 02:00:00");
            r2.setRevenue(2000);
            r2.setOrders(3);
            
            AdminHourlyAggRow r3 = new AdminHourlyAggRow();
            r3.setBucketHour("2025-08-06 03:00:00");
            r3.setRevenue(100);
            r3.setOrders(3);
            doReturn(List.of(r1, r2, r3)).when(adminDashboardMapper)
                    .aggTodayHourly(any(LocalDateTime.class), any(LocalDateTime.class));

            LocalDateTime fixed = LocalDateTime.of(2025, 8, 6, 3, 51, 10);
            try (MockedStatic<LocalDateTime> dateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                dateTimeMock.when(LocalDateTime::now).thenReturn(fixed);

                AdminDashboardDto dto = adminDashboardService.getOverview(DashboardRange.TODAY);

                assertThat(dto.getLabels()).containsExactly(
                        "2025-08-06 00:00:00",
                        "2025-08-06 01:00:00",
                        "2025-08-06 02:00:00",
                        "2025-08-06 03:00:00");
                assertThat(dto.getRevenue()).containsExactly(
                        1000,
                        0,
                        2000,
                        100);
                assertThat(dto.getOrders()).containsExactly(
                        1,
                        0,
                        3,
                        3);
                assertThat(dto.getAov()).containsExactly(
                        1000,
                        null,
                        667,
                        33);
            }

        }

        @Test
        void getOverview_last7Days() {
            AdminDailyAggRow r1 = new AdminDailyAggRow();
            r1.setDay(LocalDate.of(2025, 7, 31));
            r1.setRevenue(100);
            r1.setOrders(1);
            
            AdminDailyAggRow r2 = new AdminDailyAggRow();
            r2.setDay(LocalDate.of(2025, 8, 2));
            r2.setRevenue(3000);
            r2.setOrders(7);
            
            AdminDailyAggRow r3 = new AdminDailyAggRow();
            r3.setDay(LocalDate.of(2025, 8, 5));
            r3.setRevenue(2000);
            r3.setOrders(5);
            LocalDate fixed = LocalDate.of(2025, 8, 6);

            try (MockedStatic<LocalDate> localDateMock = Mockito.mockStatic(LocalDate.class,
                    Mockito.CALLS_REAL_METHODS)) {
                localDateMock.when(LocalDate::now).thenReturn(fixed);
                doReturn(List.of(r1, r2, r3)).when(adminDashboardMapper).aggDaily(any(), any());

                AdminDashboardDto dto = adminDashboardService.getOverview(DashboardRange.LAST_7_DAYS_INCLUDING_TODAY);

                assertThat(dto.getLabels()).containsExactly(
                        "2025-07-31",
                        "2025-08-01",
                        "2025-08-02",
                        "2025-08-03",
                        "2025-08-04",
                        "2025-08-05",
                        "2025-08-06");
                assertThat(dto.getRevenue()).containsExactly(
                        100,
                        0,
                        3000,
                        0,
                        0,
                        2000,
                        0);
                assertThat(dto.getOrders()).containsExactly(
                        1,
                        0,
                        7,
                        0,
                        0,
                        5,
                        0);
                assertThat(dto.getAov()).containsExactly(
                        100,
                        null,
                        429,
                        null,
                        null,
                        400,
                        null);
            }
        }

        @Test
        void getOverview_monthToDate() {
            LocalDate fixed = LocalDate.of(2025, 8, 6);

            try (MockedStatic<LocalDate> localDateMock = Mockito.mockStatic(LocalDate.class,
                    Mockito.CALLS_REAL_METHODS)) {
                localDateMock.when(LocalDate::now).thenReturn(fixed);
                doReturn(Collections.EMPTY_LIST).when(adminDashboardMapper).aggDaily(any(), any());

                adminDashboardService.getOverview(DashboardRange.MONTH_TO_DATE);

                verify(adminDashboardMapper).aggDaily(YearMonth.from(fixed).atDay(1), fixed.plusDays(1));
            }
        }
    }

}
