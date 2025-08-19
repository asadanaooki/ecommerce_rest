package com.example.controller;

import java.util.Optional;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
     * ページング
     * reqをMapperまで渡しても現状問題ないが、将来的に詰め替えた方がいい？
     */

    private final CartService cartService;

    private final CookieUtil cookieUtil;

    // ==== 協調用の属性/ヘッダ定義（Interceptor と共有） ====
    public static final String ATTR_CART_REISSUED = "cart.reissued"; // String(newId)
    public static final String ATTR_CART_TOUCH = "cart.touch"; // Boolean TRUE

    public static final String HEADER_CART_EVENT = "X-Cart-Event";
    public static final String EVENT_CART_EXPIRED = "EXPIRED";

    @GetMapping
    public CartDto showCart(@AuthenticationPrincipal String userId,
            HttpServletRequest req,
            HttpServletResponse res) {
        // 参照：無ければ空DTO（発行しない）
        try {
            String cartId = findExistingCartId(userId, req);
            return (cartId == null) ? new CartDto() : cartService.showCart(cartId);
        } catch (ResponseStatusException e) {
            res.addHeader(HEADER_CART_EVENT, EVENT_CART_EXPIRED);
            return new CartDto();
        }
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Void> addToCart(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AddCartRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {
        // 必ずcartIdを確保（ゲストはCookie発行、会員はDB作成）
        String candidate = (userId == null)
                ? cookieUtil.extractCartId(httpReq).orElse(null)
                : cartService.getOrCreateCartId(userId);

        Optional<String> newId = cartService.addToCart(candidate, userId, productId, req);
        newId.ifPresentOrElse(id -> signalCartReissued(id, httpReq, httpRes),
                () -> signalGuestTouch(userId, httpReq));

        return ResponseEntity.ok().build();
    }

    @PatchMapping("items/{productId}/quantity")
    public ResponseEntity<Void> changeQty(@PathVariable @HexUuid @NotBlank String productId,
            @RequestBody @Min(1) @Max(20) int qty,
            @AuthenticationPrincipal String userId,
            HttpServletRequest req,
            HttpServletResponse res) {
        String candidate = findExistingCartId(userId, req);
        cartService.changeQty(candidate, userId, productId, qty);
        signalGuestTouch(userId, req);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable @HexUuid @NotBlank String productId,
            @AuthenticationPrincipal String userId,
            HttpServletRequest req,
            HttpServletResponse res) {
        String cartId = findExistingCartId(userId, req);
        cartService.removeItem(cartId, productId);
        signalGuestTouch(userId, req);

        return ResponseEntity.ok().build();
    }

    private String findExistingCartId(String userId, HttpServletRequest req) {
        return (userId == null)
                ? cookieUtil.extractCartId(req).orElse(null)
                : cartService.findUserCartId(userId).orElse(null);
    }

    private void signalCartReissued(String newId, HttpServletRequest req, HttpServletResponse res) {
        req.setAttribute(ATTR_CART_REISSUED, newId);
        res.addHeader(HEADER_CART_EVENT, EVENT_CART_EXPIRED);
    }

    private void signalGuestTouch(String userId, HttpServletRequest req) {
        if (userId == null) {
            req.setAttribute(ATTR_CART_TOUCH, Boolean.TRUE);
        }
    }

}
