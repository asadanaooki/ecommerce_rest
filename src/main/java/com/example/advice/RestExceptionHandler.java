package com.example.advice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.error.BusinessException;
import com.example.error.FieldErrorInfo;
import com.example.response.ApiResponse;

import lombok.AllArgsConstructor;

@RestControllerAdvice
@AllArgsConstructor
public class RestExceptionHandler {
    // TODO:
    // 文言のパラメータ返すことを検討
    // 同一グループ内の@AssertTrueに優先度を持たせる

    private static final Map<String, Integer> PRIORITY = Map.ofEntries(
            // 必須系
            Map.entry("NotBlank", 10),
            Map.entry("NotNull", 10),
            Map.entry("NotEmpty", 10),
            // 長さ
            Map.entry("Size", 20),
            Map.entry("Length", 20),
            // 形式
            Map.entry("Pattern", 30),
            Map.entry("EmailFormat", 30),
            // 範囲
            Map.entry("Min", 40),
            Map.entry("Max", 40),
            Map.entry("Positive", 40),
            Map.entry("PositiveOrZero", 40),
            Map.entry("Past", 40),
            // 複合
            Map.entry("AssertTrue", 50));

    private static final Map<String, String> RULE_KEY_TO_FIELD = Map.ofEntries(
            Map.entry("STOCK_GE_RESERVED", "stock_reserved_relation"),
            Map.entry("ACCEPT_TOS_REQUIRED", "acceptTos"),
            Map.entry("SHIPPING_REQUIRED_WHEN_GIFT", "shipping_group"),
            Map.entry("AVAILABLE_RANGE_VALID", "available_range"),
            Map.entry("PRICE_RANGE_VALID", "price_range"),
            Map.entry("STOCK_RANGE_VALID", "stock_range"),
            Map.entry("CREATED_RANGE_VALID", "created_range"),
            Map.entry("UPDATED_RANGE_VALID", "updated_range"),
            Map.entry("ITEMS_AND_DELETED_DISJOINT", "deleted_items"),
            Map.entry("PUBLISH_REQUIREMENTS", "publish_requirements"),
            Map.entry("PASSWORDS_MATCH", "confirmPassword"));

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ApiResponse(e.getErrorCode(), e.getData()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, Slot> best = new LinkedHashMap<>();

        e.getConstraintViolations().forEach(v -> {
            String propPath = v.getPropertyPath().toString();
            String rawField = propPath.contains(".")
                    ? propPath.substring(propPath.lastIndexOf('.') + 1)
                    : propPath;

            String ann = v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            String code = "AssertTrue".equals(ann) ? v.getMessageTemplate() : ann;

            String field = resolveField(rawField, ann, code);
            int pr = PRIORITY.get(ann);

            best.merge(field, new Slot(code, pr),
                    (oldS, newS) -> newS.priority < oldS.priority ? newS : oldS);
        });

        List<FieldErrorInfo> errors = best.entrySet().stream()
                .map(b -> new FieldErrorInfo(b.getKey(), b.getValue().code))
                .toList();

        return ResponseEntity.badRequest().body(new ApiResponse("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, Slot> best = new LinkedHashMap<>();

        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            String rawField = fe.getField();
            String ann = fe.getCode();
            String code = "AssertTrue".equals(ann) ? fe.getDefaultMessage() : ann;

            String field = resolveField(rawField, ann, code);
            int pr = PRIORITY.get(ann);

            best.merge(field, new Slot(code, pr),
                    (oldS, newS) -> newS.priority < oldS.priority ? newS : oldS);
        }

        List<FieldErrorInfo> errors = best.entrySet().stream()
                .map(b -> new FieldErrorInfo(b.getKey(), b.getValue().code))
                .toList();

        return ResponseEntity.badRequest().body(new ApiResponse("VALIDATION_ERROR", errors));
    }

    private static String resolveField(String rawField, String ann, String code) {
        if ("AssertTrue".equals(ann)) {
            return RULE_KEY_TO_FIELD.get(code);
        }
        return rawField;
    }

    @AllArgsConstructor
    private static final class Slot {
        private final String code;
        private final int priority;
    }

}
