package com.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.example.controller.CartController;
import com.example.util.CookieUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartCookieTouchInterceptor implements HandlerInterceptor {

    private final CookieUtil cookieUtil;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        if (request.getMethod().equalsIgnoreCase("GET")) {
            return;
        }
        
        Object reissued = request.getAttribute(CartController.ATTR_CART_REISSUED);
        if (reissued instanceof String newId) {
            cookieUtil.issueCartId(newId, response);
            return;
        }
        if (Boolean.TRUE.equals(request.getAttribute(CartController.ATTR_CART_TOUCH))) {
            cookieUtil.touchCartCookieIfPresent(request, response);
        }
    }
}
