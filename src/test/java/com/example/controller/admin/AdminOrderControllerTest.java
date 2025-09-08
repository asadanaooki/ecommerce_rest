package com.example.controller.admin;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.dto.admin.AdminPdfFileDto;
import com.example.dto.admin.AdminOrderDetailDto;
import com.example.service.OrderCommandService;
import com.example.service.admin.AdminOrderService;
import com.example.testConfig.CommonMockConfig;

@Import(CommonMockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminOrderService adminOrderService;

    @MockitoBean
    OrderCommandService orderCommandService;

    @Nested
    class EditOrder {
        @ParameterizedTest
        @MethodSource("provideValidArguments")
        void editOrder_success(Map<String, Integer> items, List<String> deleted) throws Exception {
            mockMvc.perform(patch("/admin/order/{orderId}/edit", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(items, deleted)))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidArguments")
        void editOrder_fail(Map<String, Integer> items, List<String> deleted,
                String expField, String expCode) throws Exception {
            mockMvc.perform(patch("/admin/order/{orderId}/edit", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(items, deleted)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].field").value(expField))
                    .andExpect(jsonPath("$.data[0].errorCode").value(expCode));
        }

        static Stream<Arguments> provideValidArguments() {
            return Stream.of(
                    Arguments.of(Map.of("p1", 3), Collections.EMPTY_LIST),
                    Arguments.of(Map.of("p1", 1), Collections.EMPTY_LIST),
                    Arguments.of(Map.of("p1", 3), List.of("p3")),
                    Arguments.of(Map.of("p1", 3, "p2", 1), List.of("p3")));

        }

        static Stream<Arguments> provideInvalidArguments() {
            return Stream.of(
                    Arguments.of(Collections.EMPTY_MAP, Collections.EMPTY_LIST, "items", "NotEmpty"),
                    Arguments.of(Map.of("", 3), Collections.EMPTY_LIST, "items[]", "NotBlank"),
                    Arguments.of(Map.of("p1", 0), List.of("p3"), "items[p1]", "Min"),
                    Arguments.of(Map.of("p1", 3), List.of(""), "deleted[0]", "NotBlank"),
                    Arguments.of(Map.of("p1", 3, "p2", 1), List.of("p1"), "deleted_items",
                            "ITEMS_AND_DELETED_DISJOINT"));

        }

        private String toJson(Map<String, Integer> items, List<String> deleted) {
            String itemsPart = items.entrySet().stream()
                    .map(e -> "\"%s\": %d".formatted(e.getKey(), e.getValue()))
                    .collect(Collectors.joining(", "));
            String deletedPart = deleted.stream()
                    .map(s -> "\"%s\"".formatted(s))
                    .collect(Collectors.joining(", "));

            return """
                    {
                      "items": {%s },
                      "deleted": [%s ]
                    }
                    """.formatted(itemsPart, deletedPart);
        }
    }

    @Test
    void getDetail() throws Exception {
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        AdminOrderDetailDto dto = new AdminOrderDetailDto();
        org.springframework.test.util.ReflectionTestUtils.setField(dto, "orderId", orderId);
        org.springframework.test.util.ReflectionTestUtils.setField(dto, "itemsSubtotalIncl", 5000);
        org.springframework.test.util.ReflectionTestUtils.setField(dto, "shippingFeeIncl", 500);
        org.springframework.test.util.ReflectionTestUtils.setField(dto, "grandTotalIncl", 5500);

        doReturn(dto).when(adminOrderService).findDetail(orderId);

        mockMvc.perform(get("/admin/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.itemsSubtotalIncl").value(5000))
                .andExpect(jsonPath("$.shippingFeeIncl").value(500))
                .andExpect(jsonPath("$.grandTotalIncl").value(5500));
    }
    
    @Test
    void download() throws Exception {
        String orderId = "550e8400-e29b-41d4-a716-446655440000";
        String fileName = "納品書_1234.pdf";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        byte[] bytes = new byte[] {1,2,3,4};
        
        AdminPdfFileDto dto = new AdminPdfFileDto(fileName, bytes);

        doReturn(dto).when(adminOrderService).generateDeliveryNote(orderId);

        mockMvc.perform(get("/admin/order/{orderId}/delivery-note", orderId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encoded))
                .andExpect(content().bytes(bytes));
    }

}
