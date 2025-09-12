package com.example.bind.editor;

import java.beans.PropertyEditorSupport;

public class NumberFallbackEditor extends PropertyEditorSupport {

    // null ⇒ 無効時は null（参照型向け）
    private final Integer fallback;

    private final int min;

    private final int max;
    
    
    public NumberFallbackEditor(Integer fallback, int min, int max) {
        this.fallback = fallback;
        this.min = min;
        this.max = max;
    }


    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.strip().isBlank()) {
            setValue(fallback);
            return;
        }
        try {
            int v = Integer.parseInt(text.strip());
            setValue((v < min || v > max) ? fallback : v);
        } catch (NumberFormatException e) {
            setValue(fallback);
        }
    }
    
    
}
