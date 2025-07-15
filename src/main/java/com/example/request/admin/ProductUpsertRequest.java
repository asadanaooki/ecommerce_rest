package com.example.request.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import com.example.enums.SaleStatus;

import lombok.Data;

@Data
public class ProductUpsertRequest {

    @NotBlank
    @Size(max = 100)
    private String productName;

    @Positive
    private Integer price;

    @Size(max = 1000)
    private String productDescription;

    @PositiveOrZero
    private Integer stock;

    @NotNull
    private SaleStatus status;

    private MultipartFile image;

    
    @AssertTrue
    private boolean isValidForPublish() {
        // TODO:
        // status増えたら != にしたほうがよいかも
        if (status == SaleStatus.UNPUBLISHED) {
            return true;
        }
        // TODO:
        // imageが0バイトがどいういうものか実験する→!image.isEmptyが必要
        // 商品説明が空白の場合弾いたほうが良いかも
        // どのフィールドでエラーはいたらわかるようにしたい
        return  ( price != null
               && stock != null
               && productDescription != null
               && image != null);
        
    }

}
