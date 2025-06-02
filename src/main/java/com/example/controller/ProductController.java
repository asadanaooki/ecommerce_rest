package com.example.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.ProductListDto;
import com.example.enums.SortType;
import com.example.security.JwtUserDetails;
import com.example.service.ProductService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class ProductController {
    // TODO
    //・トップページを/productにするか？
    //・パラメータの受け取りを個別に書いてるが、Formとかで受け取るべきか？
    //・splitの正規表現で[\\s　]+だと ブラウザの &nbsp; や Word の “改行したくない空き” などをコピー時に対応していない
    // ・パラメータ補正をtry-catchで行ってるが、パフォーマンス落ちるか？
    //・View作成するとき、DBからデータ取得するのか、Controllerで仮データを返すのか？

    private final ProductService productService;

    @GetMapping("/product")
    public ProductListDto searchProducts(
            @RequestParam(defaultValue = "1") String page,
            @RequestParam(defaultValue = "NEW") String sort,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal JwtUserDetails user) {
        SearchParam param = adjustSearchParam(page, sort, q);
//        String userId = user == null ? null : user.getUserId();
        String userId = null;
        return productService.searchProducts(param.page, param.sort, param.keywords, userId);

    }

    //    @PostMapping("/api/product/{productId}/favorite/add")
    //    public ResponseEntity<Void> addFavorite(@PathVariable String productId) {
    //        productService.addFavorite(productId);
    //
    //        return ResponseEntity.ok().build();
    //    }
    //
    //    @PostMapping("/api/product/{productId}/favorite/delete")
    //    public ResponseEntity<Void> deleteFavorite(@PathVariable String productId) {
    //        productService.deleteFavorite(productId);
    //
    //        return ResponseEntity.ok().build();
    //    }
    //
    //    @GetMapping("/product/{productId}")
    //    public String showProductDetail(@PathVariable String productId, Model model) {
    //        ProductDetailDto dto = productService.getProductDetail(productId);
    //
    //        model.addAttribute("dto", dto);
    //        return "product-detail";
    //    }
    //    
    //    @PostMapping("/api/cart")
    //    public ResponseEntity<Void> addToCart(@Valid AddCartRequest req, HttpSession session){
    //        Cart cart = (Cart) session.getAttribute("CART");
    //        if(cart == null) {
    //            cart = new Cart();
    //            session.setAttribute("CART", cart);
    //        }
    //        productService.addToCart(cart, req.getProductId(), req.getQty());
    //        
    //        return ResponseEntity.ok().build();
    //    }

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
