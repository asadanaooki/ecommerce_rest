package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.ProductCardDto;
import com.example.dto.ProductDetailDto;
import com.example.dto.ProductListDto;
import com.example.enums.SortType;
import com.example.mapper.FavoriteMapper;
import com.example.mapper.ProductMapper;
import com.example.util.PaginationUtil;
import com.example.util.TaxCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    /*
     *     // TODO:
    ・総件数　XX件～XX件目表示を返す
    ・お気に入りフラグ取得するのに、JOINか2回にわけるか比較
    ・毎回手動で税込み価格計算するか？→ MapStruct検討
    ・カート追加時、別スレッドとの競合　 @Transactionalつけてない
    */

    private final ProductMapper productMapper;

    private final FavoriteMapper favoriteMapper;

    @Value("${settings.product.size}")
    private int pageSize;

    @Value("${settings.product.page-nav-radius}")
    private int radius;

    private final TaxCalculator taxCalculator;

    public ProductListDto searchProducts(int page, SortType sort, List<String> keywords, String userId) {
        int offset = PaginationUtil.calculateOffset(page, pageSize);

        List<ProductCardDto> products = productMapper
                .searchProducts(new ProductMapper.SearchCondition(userId, keywords, sort, pageSize, offset));
        // 税込み価格へ変換
        products.forEach(p -> p.setPrice(taxCalculator.calculatePriceIncludingTax(p.getPrice())));

        int totalCount = productMapper.countProducts(keywords);

        int totalPage = PaginationUtil.calculateTotalPage(totalCount, pageSize);
        List<Integer> pageNumbers = PaginationUtil.createPageNumbers(page, totalPage, radius);

        return new ProductListDto(products, totalPage, pageNumbers);
    }

    public ProductDetailDto getProductDetail(String productId, String userId) {
        ProductDetailDto dto = Optional.ofNullable(productMapper.findProductDetail(productId, userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        dto.setPrice(taxCalculator.calculatePriceIncludingTax(dto.getPrice()));

        return dto;
    }
}
