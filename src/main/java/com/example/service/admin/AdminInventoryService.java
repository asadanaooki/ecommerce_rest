package com.example.service.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.admin.AdminInventoryDetailDto;
import com.example.dto.admin.AdminInventoryDto;
import com.example.dto.admin.AdminInventoryListDto;
import com.example.enums.StockStatus;
import com.example.mapper.admin.AdminInventoryMapper;
import com.example.request.admin.InventoryAdjustRequest;
import com.example.request.admin.InventorySearchRequest;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminInventoryService {
    // TODO:
    // applyStockStatusはユーザー向けの商品画面でも使うかも。共通化検討
    // 発注画面
    //    第1段階（最小）：表示 ➜ ロック ➜ 手動調整
    //    第2段階：フィルタ／並び替え／検索
    //    第3段階：CSVエクスポート・ページネーション・監査ログ
    //    第4段階（拡張）：商品別閾値・在庫履歴・倉庫別在庫
    // 在庫手動調整で、同時編集や在庫変動があった場合の整合性どうするか。例：バージョンカラムなど

    private final AdminInventoryMapper adminInventoryMapper;

    @Value("${settings.admin.inventory.size}")
    private int pageSize;

    @Value("${settings.admin.inventory.low-stock-threshold}")
    private int threshold;

    public AdminInventoryListDto search(InventorySearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);
        List<AdminInventoryDto> list = adminInventoryMapper.search(req, threshold, pageSize, offset);
        for (AdminInventoryDto dto : list) {
            dto.setStockStatus(resolveStockStatus(dto.getAvailable()));
        }

        return new AdminInventoryListDto(
                list,
                adminInventoryMapper.count(req, threshold),
                pageSize);
    }

    public AdminInventoryDetailDto getDetail(String productId) {
        AdminInventoryDetailDto dto = adminInventoryMapper.find(productId);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        dto.setStockStatus(resolveStockStatus(dto.getAvailable()));
        return dto;
    }
    
    public void adjust(String productId, InventoryAdjustRequest req) {
       int rows = adminInventoryMapper.updateInventory(productId, req);
       if (rows == 0) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND);
       }
    }

    private StockStatus resolveStockStatus(int available) {
        // 防御的に<=0としている
        if (available <= 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (available <= threshold) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.IN_STOCK;
        }
    }
}
