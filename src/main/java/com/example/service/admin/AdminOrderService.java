package com.example.service.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.dto.admin.AdminOrderDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.enums.PaymentStatus;
import com.example.enums.ShippingStatus;
import com.example.mapper.admin.AdminOrderMapper;
import com.example.request.admin.OrderSearchRequest;
import com.example.util.PaginationUtil;
import com.example.util.TaxCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    // TODO:
    // 伝票作成でのPDF出力
    // CSV出力
    // 検索の便利さのために、カナも表示したほうがよいかも
    // OrderDetailDtoを戻り値にすると、一時的にitemsがnullになる。未完全Dtoは防ぐ？
    // 税込み価格の変換忘れてしまう、早めに対策する
    // ステータスの状態遷移ルールや定義。手動から自動遷移にしたい
    // Webhook or ポーリング
    // サーバー側の防御策としての SQL ガード(現在のステータスと違うときだけ更新)入れるか

    private final AdminOrderMapper adminOrderMapper;

    @Value("${settings.admin.order.size}")
    private int pageSize;

    private final TaxCalculator calculator;

    public AdminOrderListDto search(OrderSearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);

        List<AdminOrderDto> content = adminOrderMapper.selectPage(req, pageSize, offset);
        content.forEach(d -> d.setTotalPrice(calculator.calculatePriceIncludingTax(d.getTotalPrice())));
        int total = adminOrderMapper.count(req);

        return new AdminOrderListDto(content, total, pageSize);
    }

    public AdminOrderDetailDto findDetail(String orderId) {
        // 既にDBには税込み価格が入ってるため、変換不要
        // checkout時、税込みで保存してるため
        AdminOrderDetailDto dto = adminOrderMapper.selectOrderHeader(orderId);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<AdminOrderDetailItemDto> items = adminOrderMapper.selectOrderItems(orderId);
        dto.setItems(items);

        return dto;
    }
    
    public void changeShippingStatus(String orderId, ShippingStatus status) {
        adminOrderMapper.updateShippingStatus(orderId, status);
    }
    
    public void changePaymentStatus(String orderId, PaymentStatus status) {
        adminOrderMapper.updatePaymentStatus(orderId, status);
    }
}
