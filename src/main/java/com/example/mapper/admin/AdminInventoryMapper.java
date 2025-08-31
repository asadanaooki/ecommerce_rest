package com.example.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.admin.AdminInventoryDetailDto;
import com.example.dto.admin.AdminInventoryDto;
import com.example.request.admin.InventorySearchRequest;

@Mapper
public interface AdminInventoryMapper {
    // TODO:
    // 引数まとめる？

    List<AdminInventoryDto> search(InventorySearchRequest req, int threshold, int limit, int offset);
    
    int count(InventorySearchRequest req, int threshold);
    
    AdminInventoryDetailDto find(String productId);
    
}
