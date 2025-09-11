package com.example.feature.review;

import static org.assertj.core.api.Assertions.*;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.enums.review.ReviewEvent;
import com.example.enums.review.ReviewStatus;
import com.example.mapper.ReviewMapper;

@ExtendWith(MockitoExtension.class)
class ReviewGuardTest {

    @Mock
    ReviewMapper reviewMapper;
    
    @InjectMocks
    ReviewGuard sut ;

    // --- ホワイトリスト（許可される from 状態） ---
    static final EnumSet<ReviewStatus> SUBMIT_ALLOWED = EnumSet.of(
            ReviewStatus.PENDING,
            ReviewStatus.REJECTED);
    static final EnumSet<ReviewStatus> APPROVE_ALLOWED = EnumSet.of(ReviewStatus.PENDING);
    static final EnumSet<ReviewStatus> REJECT_ALLOWED = EnumSet.of(ReviewStatus.PENDING);

    // 終端集合（先頭ガードで全イベント例外）
    static final EnumSet<ReviewStatus> TERMINALS = EnumSet.of(ReviewStatus.APPROVED);

    // 成功時の「次状態」マップ
    static final Map<ReviewEvent, ReviewStatus> NEXT_ON_SUCCESS = Map.of(
            ReviewEvent.SUBMIT, ReviewStatus.PENDING,
            ReviewEvent.APPROVE, ReviewStatus.APPROVED,
            ReviewEvent.REJECT, ReviewStatus.REJECTED);

    // イベントごとの NG メッセージ（終端以外のNG時）
    static final Map<ReviewEvent, String> NG_MESSAGE = new EnumMap<>(Map.of(
            ReviewEvent.SUBMIT, "Submit not allowed",
            ReviewEvent.APPROVE, "Approve not allowed",
            ReviewEvent.REJECT, "Reject not allowed"));

    
    @ParameterizedTest
    @EnumSource(ReviewStatus.class)
    void next_submit(ReviewStatus from) {
       ReviewState cur = new ReviewState(from);
        if (TERMINALS.contains(from)) {
            assertThatThrownBy(() -> sut.next(cur, ReviewEvent.SUBMIT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Terminal state");
            return;
        }
        
        if (SUBMIT_ALLOWED.contains(from)) {
            ReviewState next = sut.next(cur, ReviewEvent.SUBMIT);
            assertThat(next.getStatus()).isEqualTo(NEXT_ON_SUCCESS.get(ReviewEvent.SUBMIT));
        }else {
            assertThatThrownBy(() -> sut.next(cur, ReviewEvent.SUBMIT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(NG_MESSAGE.get(ReviewEvent.SUBMIT));
        }
    }
    
    @ParameterizedTest
    @EnumSource(ReviewStatus.class)
    void next_approve(ReviewStatus from) {
       ReviewState cur = new ReviewState(from);
        if (TERMINALS.contains(from)) {
            assertThatThrownBy(() -> sut.next(cur, ReviewEvent.APPROVE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Terminal state");
            return;
        }
        
        if (APPROVE_ALLOWED.contains(from)) {
            ReviewState next = sut.next(cur, ReviewEvent.APPROVE);
            assertThat(next.getStatus()).isEqualTo(NEXT_ON_SUCCESS.get(ReviewEvent.APPROVE));
        }else {
            assertThatThrownBy(() -> sut.next(cur, ReviewEvent.APPROVE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(NG_MESSAGE.get(ReviewEvent.APPROVE));
        }
    }
    
    @ParameterizedTest
    @EnumSource(ReviewStatus.class)
    void next_reject(ReviewStatus from) {
       ReviewState cur = new ReviewState(from);
        if (TERMINALS.contains(from)) {
            assertThatThrownBy(() -> sut.next(cur, ReviewEvent.REJECT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Terminal state");
            return;
        }
        
        if (REJECT_ALLOWED.contains(from)) {
            ReviewState next = sut.next(cur, ReviewEvent.REJECT);
            assertThat(next.getStatus()).isEqualTo(NEXT_ON_SUCCESS.get(ReviewEvent.REJECT));
        }else {
            assertThatThrownBy(() -> sut.next(cur, ReviewEvent.REJECT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(NG_MESSAGE.get(ReviewEvent.REJECT));
        }
    }
 
}
