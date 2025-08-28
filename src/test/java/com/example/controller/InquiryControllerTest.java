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
import com.example.util.CookieUtil;
import com.example.interceptor.CartCookieTouchInterceptor;
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

    @MockitoBean
    CookieUtil cookieUtil;

    @MockitoBean
    CartCookieTouchInterceptor cartCookieTouchInterceptor;

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
                // orderNumber
                // NULL
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNumber", null)),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNumber", "1")),
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNumber", "1234")),
                // message
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("message", "a".repeat(1000))));
    }
    
    @ParameterizedTest
    @MethodSource("provideInvalidArguments")
    void submit_failure(Consumer<Map<String, Object>> override, String expField, String expCode) throws Exception {
        Map<String, Object> m = base();
        override.accept(m);
        String json = objectMapper.writeValueAsString(m);
        
        mockMvc.perform(post("/inquiry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].field").value(expField))
        .andExpect(jsonPath("$.data[0].errorCode").value(expCode));
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
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("lastName", ""), "lastName", "NotBlank"),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("lastName", "a".repeat(51)), "lastName", "Length"),
                // firstName
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("firstName", ""), "firstName", "NotBlank"),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("firstName", "a".repeat(51)), "firstName", "Length"),
                // email
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", ""), "email", "NotBlank"),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", email255), "email", "Length"),
                // @EmailFormat
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("email", "sあmple@"), "email", "EmailFormat"),
                // phoneNumber
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", ""), "phoneNumber", "NotBlank"),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "0123456789"), "phoneNumber", "Length"),
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "012345678901"), "phoneNumber", "Length"),
                // @Pattern
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("phoneNumber", "0123456７89"), "phoneNumber", "Length"),
                // orderNumber
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNumber", ""), "orderNumber", "Length"),
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNumber", "12345"), "orderNumber", "Length"),
                // @Pattern
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("orderNumber", "123４"), "orderNumber", "Pattern"),
                // message
                // @NotBlank
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("message", ""), "message", "NotBlank"),
                // @Length
                Arguments.of((Consumer<Map<String, Object>>) m -> m.put("message", "a".repeat(1001)), "message", "Length"));
    }

    static Map<String, Object> base() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("lastName", "山田");
        m.put("firstName", "太郎");
        m.put("email", "user@example.com");
        m.put("phoneNumber", "08012345678");
        m.put("orderNumber", null);
        m.put("message", "お問い合わせです。");
        return m;
    }

}
