package com.example.util;

import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    /* TODO:
     * issueCartId
         現状、クッキー無期限
         後で属性追加
     */

    private static final String CART_COOKIE = "cartId";
    private static final int MAX_AGE_SEC = 60 * 60 * 24 * 14; // 14日

//    /**
//     * ゲスト用 cartId を返す。<br>
//     * - 既に Cookie があればその値を返す<br>
//     * - 無ければ UUID を発行し、Cookie をセットして返す
//     */
//    public String getOrCreateCartId(HttpServletRequest req, HttpServletResponse res) {
//        return extractCartId(req).orElseGet(() -> {
//            String id = UUID.randomUUID().toString();
//            issueCartId(id, res);
//            return id;
//        });
//    }

    public void issueCartId(String cartId, HttpServletResponse res) {
        Cookie ck = new Cookie(CART_COOKIE, cartId);
        ck.setPath("/");

        //            ck.setHttpOnly(true);
        //            ck.setSecure(true);            // HTTPS 運用時
        ck.setMaxAge(MAX_AGE_SEC);
        //            ck.setAttribute("SameSite", "Lax"); // Spring 6.1+
        res.addCookie(ck);
    }

    public Optional<String> extractCartId(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (CART_COOKIE.equals(c.getName())) {
                    return Optional.of(c.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public void clearCartId(HttpServletResponse res) {
        Cookie ck = new Cookie(CART_COOKIE, "");
        ck.setPath("/");
        ck.setMaxAge(0);
        res.addCookie(ck);
    }

    public void touchCartCookieIfPresent(HttpServletRequest req, HttpServletResponse res) {
        extractCartId(req).ifPresent(id -> issueCartId(id, res));
    }
}
