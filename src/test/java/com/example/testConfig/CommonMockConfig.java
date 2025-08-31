package com.example.testConfig;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.interceptor.CartCookieTouchInterceptor;
import com.example.util.CookieUtil;
import com.example.util.JwtUtil;

@TestConfiguration
public class CommonMockConfig {

    @Bean
    JwtUtil jwtUtil() {
        return Mockito.mock(JwtUtil.class);
    }

    @Bean
    CookieUtil cookieUtil() {
        return Mockito.mock(CookieUtil.class);
    }

    @Bean
    CartCookieTouchInterceptor cartCookieTouchInterceptor() {
        return Mockito.mock(CartCookieTouchInterceptor.class);
    }
}
