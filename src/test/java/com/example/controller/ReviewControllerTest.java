package com.example.controller;

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
import org.springframework.test.web.servlet.ResultActions;

import com.example.service.ReviewCommandService;
import com.example.service.ReviewService;
import com.example.testConfig.CommonMockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@Import(CommonMockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReviewService reviewService;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReviewCommandService reviewCommandService;

    @Nested
    class PostReview {
        String productId = "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68";

        @ParameterizedTest
        @MethodSource("provideValidArguments")
        void postReview_success(Consumer<Map<String, Object>> override) throws Exception {
            Map<String, Object> m = base();
            override.accept(m);

            mockMvc.perform(post("/reviews/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(m)))
                    .andExpect(status().isOk());
        }

        static Stream<Arguments> provideValidArguments() {
            return Stream.of(
                    // rating
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("rating", 1)),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("rating", 5)),
                    // title
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("title", null)),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("title", " 　 　")),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("title", "a")),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("title", "a".repeat(50))),
                    // body
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("body", null)),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("body", "\n 　 　")),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("body", "a")),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("body", "a".repeat(500))));
        }

        @ParameterizedTest
        @MethodSource("provideInvalidArguments")
        void postReview_failure(Consumer<Map<String, Object>> override,
                String expField, String expCode) throws Exception {
            Map<String, Object> m = base();
            override.accept(m);

            ResultActions result = mockMvc.perform(post("/reviews/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(m)))
                    .andExpect(status().isBadRequest());
            if (expField == null && expCode == null) {
                return;
            }
            result.andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].field").value(expField))
                    .andExpect(jsonPath("$.data[0].errorCode").value(expCode));
        }

        static Stream<Arguments> provideInvalidArguments() {
            return Stream.of(
                    // rating
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("rating", null),
                            "rating", "NotNull"),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("rating", 0),
                            "rating", "Min"),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("rating", 6),
                            "rating", "Max"),
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("rating", 4.5),
                            null, null),
                    // title
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("title", "a".repeat(51)),
                            "title", "Size"),
                    // body
                    Arguments.of((Consumer<Map<String, Object>>) m -> m.put("body", "a".repeat(501)),
                            "body", "Size"));
        }

        static Map<String, Object> base() {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("rating", 3);
            m.put("title", "normal");
            m.put("body", "body");
            return m;
        }

    }

}
