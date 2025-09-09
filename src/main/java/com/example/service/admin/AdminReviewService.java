package com.example.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dto.admin.AdminReviewDetailDto;
import com.example.dto.admin.AdminReviewListDto;
import com.example.dto.admin.AdminReviewRowDto;
import com.example.enums.review.ReviewStatus;
import com.example.mapper.admin.AdminReviewMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReviewService {
    /* TODO:
     * フィルター、ソート未実装
     * サジェスト検索(商品名、SKU)
     */
    

    private final AdminReviewMapper adminReviewMapper;
    
    
    public AdminReviewListDto getReviewSummaries(ReviewStatus status) {
        List<AdminReviewRowDto> items = adminReviewMapper.selectReviewSummaries(status);
        
        return new AdminReviewListDto(items);
    }
    
    public AdminReviewDetailDto getDetail(String productId, String userId) {
        return adminReviewMapper.selectReviewDetail(productId, userId);
    }

}
