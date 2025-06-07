package com.example.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    /*    TODO:
    ・「鍵管理」　現状ハードコードされたシークレット をコードに置いている
    ・Refresh トークン方式　現状15 分固定
    */

    private static final String SECRET = "myDevSecretKey1234567890_ABCDEFG";
    private static final long EXP = 15 * 60; // 秒（15 分）

    public String issue(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXP * 1000))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parser().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String subject(String token) {
        Jws<Claims> jws = parser().parseSignedClaims(token);
        return jws.getPayload().getSubject();
    }

    private JwtParser parser() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build();
    }
}
