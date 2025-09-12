package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Review;
import com.example.entity.User;
import com.example.enums.order.RejectReason;
import com.example.enums.review.ReviewEvent;
import com.example.enums.review.ReviewStatus;
import com.example.feature.review.ReviewGuard;
import com.example.feature.user.UserGuard;
import com.example.mapper.ReviewMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.param.ReviewUpdateParam;
import com.example.request.review.RejectReviewRequest;
import com.example.request.review.SubmitReviewRequest;
import com.example.support.MailGateway;

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @Mock
    ReviewMapper reviewMapper;
    
    @Mock
    UserMapper userMapper;
    
    @Mock
    MailGateway gateway;
    
    @InjectMocks
    ReviewCommandService reviewCommandService;
    
    final String productId = "P-001";
    final String userId = "U-001";
    
    User user;
    Review review;
    
    @BeforeEach
    void setup() {
        ReviewGuard reviewGuard = spy(new ReviewGuard(reviewMapper));
        UserGuard userGuard = spy(new UserGuard(userMapper));
        ReflectionTestUtils.setField(reviewCommandService, "reviewGuard", reviewGuard);
        ReflectionTestUtils.setField(reviewCommandService, "userGuard", userGuard);
        
        user = new User();
        user.setUserId(userId);
        user.setNickname("yamada");
        user.setEmail("user@example.com");
        user.setLastName("山田");
        user.setFirstName("太郎");
        
        review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(5);
        review.setTitle("title");
        review.setReviewText("text");
        
       lenient().doReturn(user).when(userMapper).selectUserByPrimaryKey(userId);
       lenient().doReturn(true).when(reviewMapper).hasPurchased(userId, productId);
       lenient().doReturn(review).when(reviewMapper).selectByPrimaryKey(productId, userId);
       lenient().doReturn(1).when(reviewMapper).updateByEvent(any());
    }


    @Nested
    class Submit{
        SubmitReviewRequest req;
        
        @BeforeEach
        void setup() {
            req = new SubmitReviewRequest();
            req.setRating(3);
            req.setTitle("normal");
            req.setBody("new body");
        }
        
        @Test
        void submit_noNickname() {
            user.setNickname(null);
            
            assertThatThrownBy(() -> {
                reviewCommandService.submit(productId, userId, req);
            })
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException ex = (ResponseStatusException) e;
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(ex.getReason()).isEqualTo("NICKNAME_REQUIRED");
            });
        }
        
        @Test
        void submit_notPurchaser() {
            doReturn(false).when(reviewMapper).hasPurchased(userId, productId);
            assertThatThrownBy(() -> {
                reviewCommandService.submit(productId, userId, req);
            })
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException ex = (ResponseStatusException) e;
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            });
        }
        
        @Test
        void submit_new() {
            doReturn(null).when(reviewMapper).selectByPrimaryKey(productId, userId);
            reviewCommandService.submit(productId, userId, req);
            
            verify(reviewMapper).insertReview(argThat(r ->
                    r.getProductId().equals(productId)
                    && r.getUserId().equals(userId)
                    && r.getRating()== 3
                    && r.getTitle().equals("normal")
                    && r.getReviewText().equals("new body")
                    && Stream.of(
                            r.getRejectReason(),
                            r.getRejectNote(),
                            r.getCreatedAt(),
                            r.getUpdatedAt()
                            ).allMatch(Objects::isNull)
                    
                    ));
            verify(reviewMapper, never()).updateByEvent(any());
        }
        
        @Test
        void submit_again() {
            review.setStatus(ReviewStatus.REJECTED);
            review.setRejectReason(RejectReason.SPAM);
            
            reviewCommandService.submit(productId, userId, req);
            
            ArgumentCaptor<ReviewUpdateParam> captor = ArgumentCaptor.forClass(ReviewUpdateParam.class);
            verify(reviewMapper).updateByEvent(captor.capture());
            ReviewUpdateParam p = captor.getValue();
            
            assertThat(p.getProductId()).isEqualTo(productId);
            assertThat(p.getUserId()).isEqualTo(userId);
            
            assertThat(p.getEvent()).isEqualTo(ReviewEvent.SUBMIT);
            assertThat(p.getFromStatus()).isEqualTo(ReviewStatus.REJECTED);
            assertThat(p.getToStatus()).isEqualTo(ReviewStatus.PENDING);
            
            assertThat(p.getRating()).isEqualTo(3);
            assertThat(p.getTitle()).isEqualTo("normal");
            assertThat(p.getBody()).isEqualTo("new body");
            
            assertThat(p.getRejectReason()).isNull();;
            assertThat(p.getRejectNote()).isNull();
        }
    }
    
    @Nested
    class Approve {
        
        @Test
        void approve_success() {
            review.setStatus(ReviewStatus.PENDING);
            
            reviewCommandService.approve(productId, userId);
            
            ArgumentCaptor<ReviewUpdateParam> captor = ArgumentCaptor.forClass(ReviewUpdateParam.class);
            verify(reviewMapper).updateByEvent(captor.capture());
            ReviewUpdateParam p = captor.getValue();
            
            assertThat(p.getProductId()).isEqualTo(productId);
            assertThat(p.getUserId()).isEqualTo(userId);
            
            assertThat(p.getEvent()).isEqualTo(ReviewEvent.APPROVE);
            assertThat(p.getFromStatus()).isEqualTo(ReviewStatus.PENDING);
            assertThat(p.getToStatus()).isEqualTo(ReviewStatus.APPROVED);
            
            assertThat(p.getRating()).isNull();
            assertThat(p.getTitle()).isNull();
            assertThat(p.getBody()).isNull();
            
            assertThat(p.getRejectReason()).isNull();;
            assertThat(p.getRejectNote()).isNull();
        }
        
        @Test
        void approve_stateInvalid() {
            review.setStatus(ReviewStatus.APPROVED);
            
            assertThatThrownBy(() -> reviewCommandService.approve(productId, userId))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException ex = (ResponseStatusException) e;
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(ex.getReason()).isEqualTo("REVIEW_STATE_INVALID");
            });
            
            verify(reviewMapper, never()).updateByEvent(any());
        }
        
        @Test
        void approve_updateConflict() {
            doReturn(0).when(reviewMapper).updateByEvent(any());
            review.setStatus(ReviewStatus.PENDING);
            
            assertThatThrownBy(() -> reviewCommandService.approve(productId, userId))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException ex = (ResponseStatusException) e;
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(ex.getReason()).isEqualTo("REVIEW_STATE_CONFLICT");
            });
        }
    }
    
    @Nested
    class Reject {
        
        @Test
        void reject() {
            review.setStatus(ReviewStatus.PENDING);
            
            RejectReviewRequest req = new RejectReviewRequest();
            req.setReason(RejectReason.SPAM);
            req.setNote("bad");
            
            reviewCommandService.reject(productId, userId, req);
            
            ArgumentCaptor<ReviewUpdateParam> captor = ArgumentCaptor.forClass(ReviewUpdateParam.class);
            verify(reviewMapper).updateByEvent(captor.capture());
            ReviewUpdateParam p = captor.getValue();
            
            assertThat(p.getProductId()).isEqualTo(productId);
            assertThat(p.getUserId()).isEqualTo(userId);
            
            assertThat(p.getEvent()).isEqualTo(ReviewEvent.REJECT);
            assertThat(p.getFromStatus()).isEqualTo(ReviewStatus.PENDING);
            assertThat(p.getToStatus()).isEqualTo(ReviewStatus.REJECTED);
            
            assertThat(p.getRating()).isNull();
            assertThat(p.getTitle()).isNull();
            assertThat(p.getBody()).isNull();
            
            assertThat(p.getRejectReason()).isEqualTo(RejectReason.SPAM);
            assertThat(p.getRejectNote()).isEqualTo("bad");
        }
    }
}
