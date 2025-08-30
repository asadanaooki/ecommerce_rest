package com.example.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.admin.AdminReviewDetailDto;
import com.example.dto.admin.AdminReviewRowDto;
import com.example.enums.review.ReviewStatus;

@Mapper
public interface AdminReviewMapper {
    
    List<AdminReviewRowDto> selectReviewSummaries(ReviewStatus status);
    
    AdminReviewDetailDto selectReviewDetail(String productId, String userId);
}
