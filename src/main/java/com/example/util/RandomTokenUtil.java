package com.example.util;

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
    
    private RandomTokenUtil(){}
}
