package com.example.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.request.ReviewPostRequest;
import com.example.service.ReviewService;
import com.example.util.JwtUtil;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReviewService reviewService;
    
    @MockitoBean
    JwtUtil jwtUtil;
    
    @Nested
    class PostReview{
        String productId = "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68";
        
        @ParameterizedTest
        @MethodSource("provideArguments")
        void postReview_parameter(Integer rating, String reviewText, boolean expected) throws Exception {
            mockMvc.perform(post("/review/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "rating": "%s",
                              "reviewText": "%s"
                            }
                            """.formatted(rating, reviewText)))
            .andExpect(expected ? status().isOk() : status().isBadRequest());
        }

            static Stream<Arguments> provideArguments(){
                return Stream.of(
                        // rating
                        Arguments.of(0, "test", false),
                        Arguments.of(1, "test", true),
                        Arguments.of(5, "test", true),
                        Arguments.of(6, "test", false),
                        
                        // reviewText
                        Arguments.of(2, "", true),
                        Arguments.of(2, "あ".repeat(500), true),
                        Arguments.of(2, "あ".repeat(501), false)
                        );
            }
            
            @Test
            void postReview_strip() throws Exception {
                // TODO:
                // 本来、呼び出しメソッドでユーザーIDはnullではない。
                // 実際、ユーザーIDが入るようにしてテストしたい
                mockMvc.perform(post("/review/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": "%s",
                                  "reviewText": "%s"
                                }
                                """.formatted(3, " 　　 　")))
                .andExpect(status().isOk());
                
                ArgumentCaptor<ReviewPostRequest> cap =
                        ArgumentCaptor.forClass(ReviewPostRequest.class);
                        
                verify(reviewService).postReview(anyString(), any(), cap.capture());
                assertThat(cap.getValue().getReviewText()).isNull();
            
            }
    }



}
