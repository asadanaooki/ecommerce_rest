package com.example.controller.admin;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.enums.order.RejectReason;
import com.example.service.ReviewCommandService;
import com.example.service.admin.AdminReviewService;
import com.example.testConfig.CommonMockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@Import(CommonMockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminReviewController.class)
class AdminReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminReviewService adminReviewService;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReviewCommandService reviewCommandService;

    @Nested
    class Reject {
        final String productId = "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68";
        final String userId = "2f3a7c19-2a2f-4a7e-bd5f-6a1f93a0c0aa";

        @ParameterizedTest
        @MethodSource("provideValidArguments")
        void reject_success(Consumer<Map<String, Object>> override) throws Exception {
            Map<String, Object> m = base();
            override.accept(m);

            mockMvc.perform(post("/admin/reviews/{productId}/{userId}/reject", productId, userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(m)))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidArguments")
        void reject_failure(Consumer<Map<String, Object>> override, String expField, String expCode) throws Exception {
            Map<String, Object> m = base();
            override.accept(m);

            mockMvc.perform(post("/admin/reviews/{productId}/{userId}/reject", productId, userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(m)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].field").value(expField))
                    .andExpect(jsonPath("$.data[0].errorCode").value(expCode));
        }

        static Stream<Arguments> provideInvalidArguments() {
            return Stream.of(
                    // reason
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("reason", null),
                            "reason", "NotNull"),
                    // note
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("note", "さ".repeat(501)),
                            "note", "Size"),
                    // 複合条件
                    Arguments.of((Consumer<Map<String, Object>>) m -> {
                        m.put("reason", RejectReason.OTHER);
                        m.put("note", null);
                    }, "note", "REJECT_NOTE_REQUIRED_WHEN_OTHER"));
        }

        static Stream<Arguments> provideValidArguments() {
            return Stream.of(
                    // reason & 複合条件
                    Arguments.of((Consumer<Map<String, Object>>) m -> {
                    }),
                    // note
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("note", " 　　")),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("note", "あ".repeat(500)),
                    // 複合条件
                    Arguments.of((Consumer<Map<String, Object>>) m -> {
                        m.put("reason", RejectReason.OTHER);
                        m.put("note", "abc");
                            })));
        }

        static Map<String, Object> base() {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("reason", RejectReason.INAPPROPRIATE);
            return m;
        }
    }

}
