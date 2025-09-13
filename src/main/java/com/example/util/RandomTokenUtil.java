package com.example.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class RandomTokenUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    public static String generate() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_URL.encodeToString(bytes);
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.US_ASCII));

            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private RandomTokenUtil() {
    }
}
