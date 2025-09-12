package com.example.advice;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import com.example.bind.DefaultEnumEditor;
import com.example.bind.annotation.EnumFallback;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GetEnumFallbackBinder {

    private final HttpServletRequest request;

    @InitBinder
    public void init(WebDataBinder binder) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        Class<?> target = binder.getTargetType().getRawClass();
        if (target == null) {
            return;
        }

        ReflectionUtils.doWithFields(target, f -> {
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
