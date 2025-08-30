package com.example.controller.admin;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;

import com.example.dto.admin.AdminProductListDto;
import com.example.service.admin.AdminProductService;
import com.example.testConfig.CommonMockConfig;

@Import(CommonMockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminProductService adminProductService;

    @Nested
    class SearchProducts {
        @ParameterizedTest
        @MethodSource("provideValidQueries")
        void searchProducts_valid(Map<String, String> map) throws Exception {
            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.setAll(map);
            doReturn(new AdminProductListDto()).when(adminProductService).searchProducts(any());

            mockMvc.perform(get("/admin/product").params(params)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        static Stream<Arguments> provideValidQueries() {
            return Stream.of(
                    // page
                    Arguments.of(Map.of("page", "1")),
                    // minPrice
                    Arguments.of(Map.of("minPrice", "1")),
                    // minAvailable
                    Arguments.of(Map.of("minAvailable", "1")),

                    // 価格範囲
                    Arguments.of(Map.of("minPrice", "100", "maxPrice", "100")),
                    // 在庫範囲
                    Arguments.of(Map.of("minAvailable", "300", "maxAvailable", "300")),
                    // 作成日範囲
                    Arguments.of(Map.of("createdFrom", "2025-06-01", "createdTo", "2025-06-01")),
                    // 更新日範囲
                    Arguments.of(Map.of("updatedFrom", "2025-06-01", "updatedTo", "2025-06-01")));

        }

        @ParameterizedTest
        @MethodSource("provideInvalidQueries")
        void searchProducts_invalid(Map<String, String> map) throws Exception {
            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.setAll(map);

            mockMvc.perform(get("/admin/product").params(params)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> provideInvalidQueries() {
            return Stream.of(
                    // page
                    Arguments.of(Map.of("page", "0")),
                    // minPrice
                    Arguments.of(Map.of("minPrice", "0")),
                    // minAvailable
                    Arguments.of(Map.of("minAvailable", "-1")),

                    // 価格範囲
                    Arguments.of(Map.of("minPrice", "100", "maxPrice", "50")),
                    // 在庫範囲
                    Arguments.of(Map.of("minAvailable", "3000", "maxAvailable", "300")),
                    // 作成日範囲
                    Arguments.of(Map.of("createdFrom", "2025-06-01", "createdTo", "2022-06-01")),
                    // 更新日範囲
                    Arguments.of(Map.of("updatedFrom", "2025-06-01", "updatedTo", "2021-06-01")));

        }
    }

    @Nested
    class Register {
        static final Map<String, String> BASE_PARAMS = Map.of(
                "productName", "test",
                "priceExcl", "1000",
                "productDescription", "desc",
                "status", "PUBLISHED");

        static final MockMultipartFile BASE_IMAGE = new MockMultipartFile("image", "img.jpg", "image/jpeg",
                new byte[10]);

        @ParameterizedTest
        @MethodSource("provideRegistrationValidArguments")
        void register_valid(Consumer<Map<String, String>> mutator, MockMultipartFile file) throws Exception {
            Map<String, String> params = new HashMap<String, String>(BASE_PARAMS);
            mutator.accept(params);

            MockMultipartHttpServletRequestBuilder req = (MockMultipartHttpServletRequestBuilder) multipart(
                    "/admin/product")
                            .contentType(MediaType.MULTIPART_FORM_DATA);

            if (file != null) {
                req.file(file);
            }
            for (Entry<String, String> entry : params.entrySet()) {
                String k = entry.getKey(), v = entry.getValue();
                if (v != null) {
                    req.param(k, v);
                }
            }

            mockMvc.perform(req).andExpect(status().isOk());
        }

        static Stream<Arguments> provideRegistrationValidArguments() {
            return Stream.of(
                    // 正常
                    Arguments.of((Consumer<Map<String, String>>) (m -> {
                    }), BASE_IMAGE),
                    // 非公開
                    Arguments.of((Consumer<Map<String, String>>) (m -> {
                        m.put("status", "UNPUBLISHED");
                        m.put("priceExcl", null);
                        m.put("productDescription", null);
                    }), null),

                    // 境界値
                    // 商品名
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("productName", "a".repeat(100))),
                            BASE_IMAGE),
                    // 価格
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("priceExcl", "1")), BASE_IMAGE),
                    // 商品説明
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("productDescription", "b".repeat(1000))),
                            BASE_IMAGE));
        }

        @ParameterizedTest
        @MethodSource("provideRegistrationInvalidArguments")
        void register_invalid(Consumer<Map<String, String>> mutator, String expField, String expCode) throws Exception {
            Map<String, String> params = new HashMap<String, String>(BASE_PARAMS);
            mutator.accept(params);

            MockMultipartHttpServletRequestBuilder req = ((MockMultipartHttpServletRequestBuilder) (multipart(
                    "/admin/product")
                            .contentType(MediaType.MULTIPART_FORM_DATA))).file(BASE_IMAGE);

            for (Entry<String, String> entry : params.entrySet()) {
                String k = entry.getKey(), v = entry.getValue();
                if (v != null) {
                    req.param(k, v);
                }
            }

            mockMvc.perform(req).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data[?(@.field == '" + expField + "' && @.errorCode == '" + expCode + "')]").exists());
        }

        static Stream<Arguments> provideRegistrationInvalidArguments() {
            return Stream.of(
                    // 商品名
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("productName", "")),
                            "productName", "NotBlank"),
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("productName", "a".repeat(101))),
                            "productName", "Size"),
                    // 商品説明
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("productDescription", "a".repeat(1001))),
                            "productDescription", "Size"),
                    // 価格
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("priceExcl", "0")),
                            "priceExcl", "Positive"),
                    // ステータス
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("status", null)),
                            "status", "NotNull"),

                    // 公開
                    Arguments.of((Consumer<Map<String, String>>) (m -> m.put("priceExcl", null)),
                            "publish_requirements", "PUBLISH_REQUIREMENTS"));

        }
    }
}
