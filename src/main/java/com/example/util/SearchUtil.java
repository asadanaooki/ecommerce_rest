package com.example.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchUtil {
    /* TODO
     * 入力値の正規化→現状、前後空白のトリミングのみ
     */

    public static List<String> extractKeywords(String q){
        if (q == null || q.strip().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(q.strip().split("[\\s\\p{Zs}]+"))
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
