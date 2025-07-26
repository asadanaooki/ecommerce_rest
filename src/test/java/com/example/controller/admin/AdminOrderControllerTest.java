package com.example.controller.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.service.admin.AdminOrderService;
import com.example.util.JwtUtil;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminOrderService adminOrderService;

    @MockitoBean
    JwtUtil jwtUtil;

    @ParameterizedTest
    @MethodSource("provideValidArguments")
    void editOrder_success(Map<String, Integer> items, List<String> deleted) throws Exception {
        mockMvc.perform(patch("/admin/order/{orderId}/edit", "id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(items, deleted)))
                .andExpect(status().isOk());
    }
    
    @ParameterizedTest
    @MethodSource("provideInvalidArguments")
    void editOrder_fail(Map<String, Integer> items, List<String> deleted) throws Exception {
        mockMvc.perform(patch("/admin/order/{orderId}/edit", "id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(items, deleted)))
                .andExpect(status().isBadRequest());
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
                Arguments.of(Collections.EMPTY_MAP, Collections.EMPTY_LIST),
                Arguments.of(Map.of("", 3), Collections.EMPTY_LIST),
                Arguments.of(Map.of("p1", 0), List.of("p3")),
                Arguments.of(Map.of("p1", 3), List.of("")),
                Arguments.of(Map.of("p1", 3, "p2", 1), List.of("p1")));

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
