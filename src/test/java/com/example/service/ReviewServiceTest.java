package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.dto.ReviewDto;
import com.example.dto.ReviewPageDto;
import com.example.mapper.ReviewMapper;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewMapper reviewMapper;

    @InjectMocks
    ReviewService reviewService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(reviewService, "pageSize", 2);
    }

    @Test
    void fetchReviews_noReview() {
        doReturn(BigDecimal.valueOf(0.0)).when(reviewMapper).selectAvg(anyString());
        doReturn(0).when(reviewMapper).countReviews(anyString());
        doReturn(Collections.EMPTY_LIST).when(reviewMapper).selectReviews(anyString(), anyInt(), anyInt());

        ReviewPageDto dto = reviewService.fetchReviews("productId", 1);

        assertThat(dto.getAverageRating()).isEqualTo(BigDecimal.valueOf(0.0));
        assertThat(dto.getTotalCount()).isZero();
        assertThat(dto.getPageSize()).isEqualTo(2);
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void fetchReviews_withReviews() {
        doReturn(BigDecimal.valueOf(3.4)).when(reviewMapper).selectAvg(anyString());
        doReturn(2).when(reviewMapper).countReviews(anyString());
        doReturn(List.of(
                new ReviewDto(
                        "test1", LocalDate.of(2025, 6, 28), 3, "良い", "良かった。"),
                new ReviewDto(
                        "test2", LocalDate.of(2025, 6, 23), 3, null, null)))
                                .when(reviewMapper).selectReviews(anyString(), anyInt(), anyInt());

        ReviewPageDto dto = reviewService.fetchReviews("productId", 1);

        assertThat(dto.getAverageRating()).isEqualTo(BigDecimal.valueOf(3.4));
        assertThat(dto.getTotalCount()).isEqualTo(2);
        assertThat(dto.getPageSize()).isEqualTo(2);
        assertThat(dto.getItems()).hasSize(2);

        assertThat(dto.getItems().get(0))
                .extracting(ReviewDto::getNickname,
                        ReviewDto::getCreatedDate,
                        ReviewDto::getRating,
                        ReviewDto::getTitle,
                        ReviewDto::getReviewText)
                .containsExactly(
                        "test1",
                        LocalDate.of(2025, 6, 28),
                        3,
                        "良い",
                        "良かった。");
    }
}
