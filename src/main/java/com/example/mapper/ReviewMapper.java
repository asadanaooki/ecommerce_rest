package com.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.ReviewDto;
import com.example.entity.Review;

@Mapper
public interface ReviewMapper {

    List<ReviewDto> selectReviews(String productId, int limit, int offset);
    
    int countReviews(String productId);
    
    boolean hasPurchased(String userId, String productId);
    
    int insertReview(Review review);
}
