package com.example.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.ProductDetailDto;
import com.example.dto.ProductListDto;
import com.example.enums.SortType;
import com.example.request.AddCartRequest;
import com.example.security.CustomUserDetails;
import com.example.service.ProductService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class ProductController {
    /* TODO
・トップページを/productにするか？
・パラメータの受け取りを個別に書いてるが、Formとかで受け取るべきか？
・splitの正規表現で[\\s　]+だと ブラウザの &nbsp; や Word の “改行したくない空き” などをコピー時に対応していない
・パラメータ補正をtry-catchで行ってるが、パフォーマンス落ちるか？
・View作成するとき、DBからデータ取得するのか、Controllerで仮データを返すのか？
・サイズの追加
    */
    private final ProductService productService;
    
    PasswordEncoder encoder;

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
    public ProductDetailDto showProductDetail(@PathVariable String productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        String userId = user == null ? null : user.getUserId();
        return productService.getProductDetail(productId, userId);
    }

    @PutMapping("/product/{productId}/favorite")
    public ResponseEntity<Void> addFavorite(@PathVariable String productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        // SpringSecurityを通過してるため、必ずuserIdが取得できる
        productService.addFavorite(productId, user.getUserId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/product/{productId}/favorite")
    public ResponseEntity<Void> deleteFavorite(@PathVariable String productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        // SpringSecurityを通過してるため、ログイン済み
        productService.deleteFavorite(productId, user.getUserId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart")
    public ResponseEntity<Void> addToCart(@Valid @RequestBody AddCartRequest req,
            HttpServletRequest httpReq, HttpServletResponse httpRes) {
        String cartId = findOrCreateCartId(httpReq, httpRes);
        // TODO:
        // reqをMapperまで渡しても現状問題ないが、将来的に詰め替えた方がいい？
        productService.addToCart(cartId, req);

        return ResponseEntity.ok().build();
    }

    private String findOrCreateCartId(HttpServletRequest req, HttpServletResponse res) {
        for (Cookie c : Optional.ofNullable(req.getCookies()).orElse(new Cookie[0])) {
            if ("cartId".equals(c.getName())) {
                return c.getValue();
            }
        }
        String id = UUID.randomUUID().toString();
        Cookie ck = new Cookie("cartId", id);
        ck.setPath("/");
        // TODO: 以下、後で追加
        //        ck.setHttpOnly(true);            // JS からアクセス不可
        //        ck.setSecure(true);              // HTTPS だけ
        //        ck.setMaxAge(60 * 60 * 24 * 30); // 30 日
        //        ck.setSameSite("Lax");           // Spring 6.1 以降
        res.addCookie(ck);
        return id;
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
