package com.example.advice;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import com.example.bind.annotation.EnumFallback;
import com.example.bind.editor.DateSanitizingEditor;
import com.example.bind.editor.DefaultEnumEditor;
import com.example.bind.editor.NumberFallbackEditor;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GetQueryParamBinder {

    private final HttpServletRequest request;

    @InitBinder
    public void init(WebDataBinder binder) {
        if (!"GET".equalsIgnoreCase(request.getMethod()) ||
                binder.getTargetType() == null) {
            return;
        }
        
        // String
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue((text == null || text.strip().isBlank()) ? null : text);
            }
        });
        
        // Integer
        binder.registerCustomEditor(Integer.class,
                new NumberFallbackEditor(null, 0, Integer.MAX_VALUE));
        
        // int
        binder.registerCustomEditor(int.class, "page",
                new NumberFallbackEditor(1, 1, Integer.MAX_VALUE));
        
        // LocalDate
        binder.registerCustomEditor(LocalDate.class, new DateSanitizingEditor());

        // Enum
        ReflectionUtils.doWithFields(binder.getTargetType().getRawClass(), f -> {
            EnumFallback anno = f.getAnnotation(EnumFallback.class);
            if (!f.getType().isEnum() || anno == null) {
                return;
            }
            Class<? extends Enum> enumType = (Class<? extends Enum>) f.getType();
            
            Enum<?> fallback = null;
            if (!anno.value().isEmpty()) {
                try {
                    fallback = Enum.valueOf(enumType, anno.value());
                } catch (IllegalArgumentException e) {
                }
            }
            binder.registerCustomEditor(enumType, f.getName(),
                    new DefaultEnumEditor(enumType, fallback));
        });
    }

}
