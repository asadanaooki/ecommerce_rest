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
        doReturn(0).when(reviewMapper).countReviews(anyString());
        doReturn(Collections.EMPTY_LIST).when(reviewMapper).selectReviews(anyString(), anyInt(), anyInt());

        ReviewPageDto dto = reviewService.fetchReviews("productId", 1);

        assertThat(dto.getAverageRating()).isEqualTo(BigDecimal.ZERO);
        assertThat(dto.getTotalCount()).isZero();
        assertThat(dto.getPageSize()).isEqualTo(2);
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void fetchReviews_withReviews() {
        doReturn(2).when(reviewMapper).countReviews(anyString());
        doReturn(List.of(
                new ReviewDto(
                        "test1", LocalDate.of(2025, 6, 28), 3, "良かった。"),
                new ReviewDto(
                        "test2", LocalDate.of(2025, 6, 23), 3, null)))
                                .when(reviewMapper).selectReviews(anyString(), anyInt(), anyInt());

        ReviewPageDto dto = reviewService.fetchReviews("productId", 1);

        assertThat(dto.getAverageRating()).isEqualTo(BigDecimal.valueOf(3.0));
        assertThat(dto.getTotalCount()).isEqualTo(2);
        assertThat(dto.getPageSize()).isEqualTo(2);
        assertThat(dto.getItems()).hasSize(2);

        assertThat(dto.getItems().get(0))
                .extracting(ReviewDto::getNickname,
                        ReviewDto::getCreatedDate,
                        ReviewDto::getRating,
                        ReviewDto::getReviewText)
                .containsExactly(
                        "test1",
                        LocalDate.of(2025, 6, 28),
                        3,
                        "良かった。");
    }
    
    @Test
    void fetchReviews_averageRoundsUp() {
        ReviewDto r1 = new ReviewDto(null, null, 5, null);
        ReviewDto r2 = new ReviewDto(null, null, 4, null);
        ReviewDto r3 = new ReviewDto(null, null, 3, null);
        ReviewDto r4 = new ReviewDto(null, null, 3, null);
        
        doReturn(4).when(reviewMapper).countReviews(anyString());
        doReturn(List.of(r1,r2,r3,r4)).when(reviewMapper).selectReviews(anyString(), anyInt(), anyInt());
        
        ReviewPageDto dto = reviewService.fetchReviews("productId", 1);
        
        assertThat(dto.getAverageRating()).isEqualTo(BigDecimal.valueOf(3.8));
    }
    
    @Test
    void fetchReviews_averageRoundsDown() {
        ReviewDto r1 = new ReviewDto(null, null, 4, null);
        ReviewDto r2 = new ReviewDto(null, null, 3, null);
        ReviewDto r3 = new ReviewDto(null, null, 3, null);
        
        doReturn(3).when(reviewMapper).countReviews(anyString());
        doReturn(List.of(r1,r2,r3)).when(reviewMapper).selectReviews(anyString(), anyInt(), anyInt());
        
        ReviewPageDto dto = reviewService.fetchReviews("productId", 1);
        
        assertThat(dto.getAverageRating()).isEqualTo(BigDecimal.valueOf(3.3));
    }

}
