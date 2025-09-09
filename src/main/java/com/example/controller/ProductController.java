package com.example.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.ProductDetailDto;
import com.example.dto.ProductListDto;
import com.example.enums.SortType;
import com.example.service.ProductService;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class ProductController {
    /* TODO
     *トップページを/productにするか？
     *パラメータの受け取りを個別に書いてるが、Formとかで受け取るべきか？
     *splitの正規表現で[\\s　]+だと ブラウザの &nbsp; や Word の “改行したくない空き” などをコピー時に対応していない
     *パラメータ補正をtry-catchで行ってるが、パフォーマンス落ちるか？
     *View作成するとき、DBからデータ取得するのか、Controllerで仮データを返すのか？
     *サイズの追加
     *searchProductsで、フィルタリング追加する
     *searchProducts：DTOでパラメータ受け取るか検討
     *adjustSearchParam：[\\s\\p{Zs}]を検討
    */

    private final ProductService productService;

    @GetMapping("/product")
    public ProductListDto searchProducts(
            @RequestParam(defaultValue = "1") String page,
            @RequestParam(defaultValue = "NEW") String sort,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal String userId) {
        SearchParam param = adjustSearchParam(page, sort, q);
        return productService.searchProducts(param.page, param.sort, param.keywords, userId);
    }

    @GetMapping("/product/{productId}")
    public ProductDetailDto showProductDetail(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId) {
        return productService.getProductDetail(productId, userId);
    }

    private SearchParam adjustSearchParam(String page, String sort, String q) {
        int pageNum;
        try {
            pageNum = Integer.parseInt(page);
            if (pageNum < 1) {
                pageNum = 1;
            }
        } catch (NumberFormatException e) {
            pageNum = 1;
        }

        SortType sortType;
        try {
            sortType = SortType.valueOf(sort.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortType = SortType.NEW; // フォールバック
        }

        String raw = Optional.ofNullable(q).orElse("").trim();
        List<String> keywords = raw.isEmpty()
                ? Collections.emptyList()
                : Arrays.stream(raw.split("[\\s　]+"))
                        .toList();

        return new SearchParam(pageNum, sortType, keywords);
    }

    record SearchParam(int page, SortType sort, List<String> keywords) {
    }
}
