package com.example.service;

import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Review;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.ReviewRejectedContext;
import com.example.enums.review.ReviewEvent;
import com.example.feature.review.ReviewGuard;
import com.example.feature.review.ReviewState;
import com.example.feature.user.UserGuard;
import com.example.mapper.ReviewMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.param.ReviewUpdateParam;
import com.example.mapper.param.ReviewUpdateParam.ReviewUpdateParamBuilder;
import com.example.request.review.RejectReviewRequest;
import com.example.request.review.SubmitReviewRequest;
import com.example.support.MailGateway;
import com.example.util.UserUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewCommandService {
    /* TODO:
     * paramへの変換を毎回書いてるが、ここどう実装するのがよいか？
     * レビュー投稿後の編集を許可するか？
     * 承認→拒否(承認後に不適切な内容が見つかった場合など)はどうする？公開/非公開カラム追加を検討
     * スパム対策→現状、更新時刻だけ変わるだけで見抜けない。ログ出力？
     * NPE対策してない
     * DuplictKey対策どうするか、現状放置
     */

    private final ReviewMapper reviewMapper;

    private final UserMapper userMapper;

    private final ReviewGuard reviewGuard;
    
    private final UserGuard userGuard;

    private final MailGateway gateway;

    @Transactional
    public void submit(String productId, String userId, SubmitReviewRequest req) {
        if (userGuard.require(userId).getNickname() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "NICKNAME_REQUIRED");
        }
        if (!reviewMapper.hasPurchased(userId, productId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Review before = reviewMapper.selectByPrimaryKey(productId, userId);
        if (before == null) {
            reviewMapper.insertReview(new Review() {
                {
                    setProductId(productId);
                    setUserId(userId);
                    setRating(req.getRating());
                    setTitle(req.getTitle());
                    setReviewText(req.getBody());
                }
            });
            return;
        }

        apply(productId, userId, ReviewEvent.SUBMIT,
                b -> b.rating(req.getRating())
                        .title(req.getTitle())
                        .body(req.getBody()));
    }

    @Transactional
    public void approve(String productId, String userId) {
        apply(productId, userId, ReviewEvent.APPROVE,
                b -> {
                });
    }

    @Transactional
    public void reject(String productId, String userId, RejectReviewRequest req) {
        apply(productId, userId, ReviewEvent.REJECT,
                b -> b.rejectReason(req.getReason())
                        .rejectNote(req.getNote()));

        User u = userMapper.selectUserByPrimaryKey(userId);
        gateway.send(MailTemplate.REVIEW_REJECTED
                .build(new ReviewRejectedContext(
                        u.getEmail(),
                        UserUtil.buildFullName(u),
                        req.getReason(),
                        req.getNote())));
    }

    private void apply(String productId, String userId, ReviewEvent ev,
            Consumer<ReviewUpdateParamBuilder> consumer) {
        Review before = reviewGuard.require(productId, userId);
        final ReviewState next;

        try {
            next = reviewGuard.next(new ReviewState(before.getStatus()), ev);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "REVIEW_STATE_INVALID");
        }

        ReviewUpdateParamBuilder builder = ReviewUpdateParam.builder()
                .productId(productId)
                .userId(userId)
                .event(ev)
                .fromStatus(before.getStatus())
                .toStatus(next.getStatus());
        consumer.accept(builder);

        int updated = reviewMapper.updateByEvent(builder.build());

        if (updated <= 0) {
            // 楽観ロック競合など：更新されず → 409
            throw new ResponseStatusException(HttpStatus.CONFLICT, "REVIEW_STATE_CONFLICT");
        }
    }
}
