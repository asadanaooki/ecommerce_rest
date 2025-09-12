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
import com.example.mapper.FavoriteMapper;
import com.example.mapper.ProductMapper;
import com.example.request.ProductSearchRequest;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    /* TODO:
     *総件数　XX件～XX件目表示を返す
     *お気に入りフラグ取得するのに、JOINか2回にわけるか比較
     *カート追加時、別スレッドとの競合　 @Transactionalつけてない
     *フィルタリング実装
     */

    private final ProductMapper productMapper;

    private final FavoriteMapper favoriteMapper;

    @Value("${settings.product.size}")
    private int pageSize;

    @Value("${settings.product.page-nav-radius}")
    private int radius;


    public ProductListDto searchProducts(String userId, ProductSearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);

        List<ProductCardDto> products = productMapper
                .searchProducts(userId, req, pageSize, offset);

        int totalCount = productMapper.countProducts(req.getKeywords());

        int totalPage = PaginationUtil.calculateTotalPage(totalCount, pageSize);
        List<Integer> pageNumbers = PaginationUtil.createPageNumbers(req.getPage(), totalPage, radius);

        return new ProductListDto(products, totalPage, pageNumbers);
    }

    public ProductDetailDto getProductDetail(String productId, String userId) {
        ProductDetailDto dto = Optional.ofNullable(productMapper.findProductDetail(productId, userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return dto;
    }
}
