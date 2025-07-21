package com.example.dto.admin;

import java.time.LocalDateTime;

import com.example.enums.SaleStatus;

import lombok.Data;

@Data
public class AdminProductDetailDto {

    /** 商品ID（UUID） */
    private String productId;

    /** SKU */
    private int sku;

    /** 商品名 */
    private String productName;
    
   private String productDescription;

    /** 価格 (円) */
    private int price;

    /** 在庫数 */
    private int stock;

    /** 公開ステータス */
    private SaleStatus status;

    private LocalDateTime createdAt;
    
    /** 更新日時 */
    private LocalDateTime updatedAt;

}
