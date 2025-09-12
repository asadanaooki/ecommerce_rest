package com.example.bind.editor;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;

public class DateSanitizingEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.strip().isBlank()) {
            setValue(null);
            return;
        }
        try {
            setValue(LocalDate.parse(text.strip()));
        } catch (NumberFormatException e) {
            setValue(null);
        }
    }
    
    
}
