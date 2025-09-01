package com.example.util;

import java.util.List;
import java.util.function.ToIntFunction;

import org.springframework.stereotype.Component;

@Component
public class OrderUtil {

    public static String formatOrderNumber(int orderNumber) {
        return String.format("%04d", orderNumber);
    }
    
    public static <T> int calculateTotalQty(List<T> items, ToIntFunction<T> qtyMapper) {
        return sumBy(items, qtyMapper);
    }
    
    public static <T> int calculateTotalPriceIncl(List<T> items, ToIntFunction<T> subtotalMapper) {
        return sumBy(items, subtotalMapper);
    }
    
    public static int calculateShippingFeeIncl(int totalPriceIncl) {
        return (totalPriceIncl >= 3000) ? 0 : 500;
    }
    
    private static <T> int sumBy(List<T> items, ToIntFunction<T> mapper) {
        return items.stream().mapToInt(mapper).sum();
    }
}
