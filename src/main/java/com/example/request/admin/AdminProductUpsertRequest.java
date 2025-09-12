package com.example.request.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import com.example.enums.SaleStatus;

import lombok.Data;

@Data
public class AdminProductUpsertRequest {
    /* TODO
     * image→NULLを削除マークにしない。そもそも、現状削除処理実装されてない
     * isValidForPublishメソッド
         assertTrue→status増えたら != にしたほうがよいかも
         imageが0バイトがどいういうものか実験する→!image.isEmptyが必要
         商品説明が空白の場合弾いたほうが良いかも
         どのフィールドでエラーはいたらわかるようにしたい
     */

    @NotBlank
    @Size(max = 100)
    private String productName;

    @Positive
    private Integer priceExcl;

    @Size(max = 1000)
    private String productDescription;

    @NotNull
    private SaleStatus status;

    private MultipartFile image;

    
    @AssertTrue(message = "PUBLISH_REQUIREMENTS")
    private boolean isValidForPublish() {
        if (status == SaleStatus.UNPUBLISHED) {
            return true;
        }
        return  ( priceExcl != null
               && productDescription != null
               && image != null);
        
    }

}
