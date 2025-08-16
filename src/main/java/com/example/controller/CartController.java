package com.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CartDto;
import com.example.request.AddCartRequest;
import com.example.service.CartService;
import com.example.util.CookieUtil;
import com.example.validation.constraint.HexUuid;

import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    /*
     * TODO:
     * userIdで毎回expression書いてるが、anonyを無効にしたほうがよいのか？
     * HttpServletRequestをサービスに渡さないほうが良いかも
     * @validatedで発生した例外を400ステータスに変換して返す
     * カート内商品はすべて表示。件数多くなったら考える
     * ページ未使用
     * reqをMapperまで渡しても現状問題ないが、将来的に詰め替えた方がいい？
     */

    private final CartService cartService;

    private final CookieUtil cookieUtil;

    @GetMapping
    public CartDto showCart(@RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal String userId,
            HttpServletRequest req) {
        // 参照：無ければ空DTO（発行しない）
        String cartId = findExistingCartId(userId, req);
        return (cartId == null) ? new CartDto() : cartService.showCart(cartId);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Void> addToCart(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AddCartRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {
        // 必ずcartIdを確保（ゲストはCookie発行、会員はDB作成）
        String cartId = (userId == null)
                ? cookieUtil.getOrCreateCartId(httpReq, httpRes)
                : cartService.getOrCreateCartId(userId);
        
        cartService.addToCart(cartId,userId, productId, req);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId,
            HttpServletRequest req) {
        String cartId = findExistingCartId(userId, req);
        cartService.removeItem(cartId, userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("items/{productId}/quantity")
    public ResponseEntity<Void> changeQty(@PathVariable @HexUuid @NotBlank String productId,
            @RequestBody @Min(1) @Max(20) int qty,
            @AuthenticationPrincipal String userId,
            HttpServletRequest req) {
        String cartId = findExistingCartId(userId, req);
        cartService.changeQty(cartId, productId, qty);

        return ResponseEntity.ok().build();
    }
    
    
    private String findExistingCartId(String userId, HttpServletRequest req) {
        return (userId == null)
                ? cookieUtil.extractCartId(req).orElse(null)
                : cartService.findUserCartId(userId).orElse(null);
    }

}
