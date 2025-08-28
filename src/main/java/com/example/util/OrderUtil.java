package com.example.util;

import org.springframework.stereotype.Component;

@Component
public class OrderUtil {

    public static String formatOrderNumber(int orderNumber) {
        return String.format("%04d", orderNumber);
    }
}
