package com.example.bind.editor;

import java.beans.PropertyEditorSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultEnumEditor<T extends Enum<T>> extends PropertyEditorSupport {

    private final Class<T> type;

    private final T fallback;

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.strip().isBlank()) {
            setValue(fallback);
            return;
        }
        String s = text.strip();

        for (T c : type.getEnumConstants()) {
            if (c.name().equalsIgnoreCase(s)) {
                setValue(c);
                return;
            }
        }
        // 不明値 → 既定 or null
        setValue(fallback);

    }

}
