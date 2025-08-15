package com.example.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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

import com.example.service.InquiryService;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(InquiryController.class)
class InquiryControllerTest {
    
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InquiryService inquiryService;

    @MockitoBean
    JwtUtil jwtUtil;

    @Autowired
    ObjectMapper objectMapper;
    
    
    @ParameterizedTest
    @MethodSource("provideValidArguments")
    void submit_success(Consumer<Map<String, Object>> override) throws Exception {
        Map<String, Object> m = base();
        override.accept(m);
        String json = objectMapper.writeValueAsString(m);
        
        mockMvc.perform(post("/inquiry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk());
    }

    static Stream<Arguments> provideValidArguments() {
        String email254 = "a".repeat(63) + // ローカル部 63
                "@" +
                "b".repeat(63) + // ドメイン・ラベル 1   (63 文字)
                "." +
                "c".repeat(63) + // ドメイン・ラベル 2   (63 文字)
                "." +
                "d".repeat(62);

        return Stream.of(
                // lastName
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("lastName", "a".repeat(50))),
                // firstName
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("firstName", "a".repeat(50))),
                // email
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", email254)),
                // phoneNumber
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "01234567890")),
                // orderNo
                // NULL
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNo", null)),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNo", "1")),
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNo", "1234")),
                // message
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("message", "a".repeat(1000))));
    }
    
    @ParameterizedTest
    @MethodSource("provideInvalidArguments")
    void submit_failure(Consumer<Map<String, Object>> override) throws Exception {
        Map<String, Object> m = base();
        override.accept(m);
        String json = objectMapper.writeValueAsString(m);

        mockMvc.perform(post("/inquiry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data", hasSize(1)));
    }

    static Stream<Arguments> provideInvalidArguments() {
        String email255 = "a".repeat(64) + // ローカル部 64
                "@" +
                "b".repeat(63) + // ドメイン・ラベル 1   (63 文字)
                "." +
                "c".repeat(63) + // ドメイン・ラベル 2   (63 文字)
                "." +
                "d".repeat(62);

        return Stream.of(
                // lastName
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("lastName", "")),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("lastName", "a".repeat(51))),
                // firstName
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("firstName", "")),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("firstName", "a".repeat(51))),
                // email
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", "")),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", email255)),
                // @EmailFormat
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", "sあmple@")),
                // phoneNumber
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "")),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "0123456789")),
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "012345678901")),
                // @Pattern
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "0123456７89")),
                // orderNo
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNo", "")),
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNo", "12345")),
                // @Pattern
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNo", "123４")),
                // message
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("message", "")),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("message", "a".repeat(1001))));
    }

    static Map<String, Object> base() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("lastName", "山田");
        m.put("firstName", "太郎");
        m.put("email", "user@example.com");
        m.put("phoneNumber", "08012345678");
        m.put("orderNo", null);
        m.put("message", "お問い合わせです。");
        return m;
    }

}
