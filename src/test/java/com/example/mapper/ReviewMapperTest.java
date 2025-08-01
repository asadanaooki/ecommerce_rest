package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.example.dto.ReviewDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.Review;
import com.example.entity.User;
import com.example.testUtil.TestDataFactory;
import com.example.util.PaginationUtil;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
class ReviewMapperTest {

    @Autowired
    ReviewMapper reviewMapper;
    
    @Autowired
    CheckoutMapper checkoutMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    TestDataFactory factory;

    String productId = "09d5a43a-d24c-41c7-af2b-9fb7b0c9e049";

    LocalDateTime now;
    LocalDateTime past;

    @BeforeEach
    void setup() {
        factory.deleteReviewsByProductId(productId);

        User user = new User();
        user.setUserId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"); // 36文字 UUID
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$10$dummyHashForTests.............."); // テスト用
        user.setLastName("テスト");
        user.setFirstName("太郎");
        user.setLastNameKana("テスト");
        user.setFirstNameKana("タロウ");
        user.setPostalCode("1000001");
        user.setAddressPrefCity("東京都千代田区");
        user.setAddressArea("千代田");
        user.setAddressBlock("1-1-1");
        user.setPhoneNumber("0312345678");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setGender("M");
        user.setNickname("ali");
        userMapper.insertUser(user);

        now = LocalDateTime.of(2025, 6, 29, 0, 0);
        past = LocalDateTime.of(2025, 6, 10, 0, 0);

        factory.createReview(new Review() {
            {
                setUserId("550e8400-e29b-41d4-a716-446655440000");
                setProductId(productId);
                setRating(5);
                setReviewText("とても良い商品です！");
                setCreatedAt(now);
                setUpdatedAt(now);
            }
        });
        factory.createReview(new Review() {
            {
                setUserId("111e8400-e29b-41d4-a716-446655440111");
                setProductId(productId);
                setRating(4);
                setReviewText("コスパが高いと思います。");
                setCreatedAt(now);
                setUpdatedAt(now);
            }
        });
        factory.createReview(new Review() {
            {
                setUserId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
                setProductId(productId);
                setRating(3);
                setCreatedAt(past);
                setUpdatedAt(past);
            }
        });
    }

    @Test
    void selectReviews() {
        List<ReviewDto> reviews = reviewMapper
                .selectReviews(productId, 10, PaginationUtil.calculateOffset(1, 10));

        assertThat(reviews).hasSize(3)
                .extracting(ReviewDto::getNickname)
                .containsExactly("sato", "yamarou", "ali");

        assertThat(reviews.get(0))
                .extracting(ReviewDto::getNickname,
                        r -> r.getCreatedDate(),
                        ReviewDto::getRating,
                        ReviewDto::getReviewText)
                .containsExactly(
                        "sato",
                        LocalDate.of(2025, 6, 29),
                        4,
                        "コスパが高いと思います。");
    }
    
    @Test
    void countReviews() {
       int count = reviewMapper.countReviews(productId);
       assertThat(count).isEqualTo(3);
    }
    
    @Nested
    class HasPurchased{
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        
        @BeforeEach
        void setup() {
            // ---------- arrange ----------
            // 1) 注文ヘッダを先に作成
            String orderId = UUID.randomUUID().toString();
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setName("山田 太郎");
            order.setPostalCode("1500041");
            order.setAddress("東京都渋谷区神南1-1-1");
            order.setTotalQty(3);
            order.setTotalPrice(9600);
            checkoutMapper.insertOrderHeader(order);

            // 2) 明細リスト作成
            OrderItem it1 = new OrderItem();
            it1.setOrderId(orderId);
            it1.setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
            it1.setProductName("testA");
            it1.setQty(1);
            it1.setPrice(750);
            it1.setSubtotal(750);

            OrderItem it2 = new OrderItem();
            it2.setOrderId(orderId);
            it2.setProductId("f9c9cfb2-0893-4f1c-b508-f9e909ba5274");
            it2.setProductName("testB");
            it2.setQty(2);
            it2.setPrice(3200);
            it2.setSubtotal(6400);

            List<OrderItem> items = List.of(it1, it2);
            
            checkoutMapper.insertOrderItems(items);
            
            factory.createReview(new Review() {
                {
                    setUserId(userId);
                    setProductId("1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68");
                    setRating(5);
                    setReviewText("とても良い商品です！");
                }
            });
        }
        
        @Test
        void hasPurchased_true() {
            assertThat(reviewMapper.hasPurchased(userId, "1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68"))
            .isTrue();
        }
        
        @Test
        void hasPurchased_false() {
            assertThat(reviewMapper.hasPurchased(userId, "4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07"))
            .isFalse();
        }
    }
    


}
