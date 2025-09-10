package com.example.service.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.admin.AdminInventoryDetailDto;
import com.example.dto.admin.AdminInventoryListDto;
import com.example.dto.admin.AdminInventoryRowDto;
import com.example.enums.StockStatus;
import com.example.mapper.ProductMapper;
import com.example.mapper.admin.AdminInventoryMapper;
import com.example.request.admin.InventoryMovementRequest;
import com.example.request.admin.InventorySearchRequest;
import com.example.support.IdempotentExecutor;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminInventoryService {
    /* TODO:
    * resolveStockStatusはユーザー向けの商品画面でも使うかも。共通化検討
    * 発注画面
        第1段階（最小）：表示 ➜ ロック ➜ 手動調整
        第2段階：フィルタ／並び替え／検索
        第3段階：CSVエクスポート・ページネーション・監査ログ
        第4段階（拡張）：商品別閾値・在庫履歴・倉庫別在庫
    * 在庫手動調整で、同時編集や在庫変動があった場合の整合性どうするか。例：バージョンカラムなど
     */

    private final AdminInventoryMapper adminInventoryMapper;

    private final ProductMapper productMapper;

    private final IdempotentExecutor executor;

    @Value("${settings.admin.inventory.size}")
    private int pageSize;

    @Value("${settings.admin.inventory.low-stock-threshold}")
    private int threshold;

    public AdminInventoryListDto search(InventorySearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);
        List<AdminInventoryRowDto> list = adminInventoryMapper.search(req, threshold, pageSize, offset);
        for (AdminInventoryRowDto dto : list) {
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

    public void receiveStock(String productId, InventoryMovementRequest req, String idempotencyKey) {
        executor.run(idempotencyKey, () -> {
            int rows = productMapper.increaseStock(productId, req.getQty(), req.getVersion());
            if (rows <= 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        });
    }

    public void issueStock(String productId, InventoryMovementRequest req, String idempotencyKey) {
        executor.run(idempotencyKey, () -> {
            int rows = productMapper.decreaseStock(productId, req.getQty(), req.getVersion());
            if (rows <= 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        });
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
