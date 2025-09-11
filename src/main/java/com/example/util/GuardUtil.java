package com.example.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GuardUtil {

    public static <T> T ensureFound(T e, String code) {
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, code);
        }
        return e;
    }
}
