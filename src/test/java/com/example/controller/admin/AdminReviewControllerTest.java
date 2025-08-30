package com.example.controller.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    @Test
    void test() {
        fail("まだ実装されていません");
    }

}
