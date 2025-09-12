package com.example.mapper.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.dto.admin.AdminInventoryDetailDto;
import com.example.dto.admin.AdminInventoryRowDto;
import com.example.request.admin.AdminInventorySearchRequest;

@Mapper
public interface AdminInventoryMapper {
    /* TODO:
     * 引数まとめる？
     */

    List<AdminInventoryRowDto> search(AdminInventorySearchRequest req, int threshold, int limit, int offset);
    
    int count(AdminInventorySearchRequest req, int threshold);
    
    AdminInventoryDetailDto find(String productId);
    
}
