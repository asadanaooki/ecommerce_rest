package com.example.util;

import java.util.List;
import java.util.function.ToIntFunction;

import org.springframework.stereotype.Component;

@Component
public class OrderUtil {

    public static String formatOrderNumber(int orderNumber) {
        return String.format("%04d", orderNumber);
    }
    
    public static int calculateShippingFeeIncl(int itemsSubtotalIncl) {
        return (itemsSubtotalIncl >= 3000) ? 0 : 500;
    }
    
    public static int calculateGrandTotalIncl(int itemsSubtotalIncl, int shippingFeeIncl) {
        return itemsSubtotalIncl + shippingFeeIncl;
    }
    
    public static <T> int sumBy(List<T> items, ToIntFunction<T> mapper) {
        return items.stream().mapToInt(mapper).sum();
    }
}
