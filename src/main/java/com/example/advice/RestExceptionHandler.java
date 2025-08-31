package com.example.advice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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
            Map.entry("ACCEPT_TOS_REQUIRED", "acceptTos"),
            Map.entry("SHIPPING_REQUIRED_WHEN_GIFT", "shipping_group"),
            Map.entry("AVAILABLE_RANGE_VALID", "available_range"),
            Map.entry("PRICE_RANGE_VALID", "price_range"),
            Map.entry("STOCK_RANGE_VALID", "stock_range"),
            Map.entry("CREATED_RANGE_VALID", "created_range"),
            Map.entry("UPDATED_RANGE_VALID", "updated_range"),
            Map.entry("ITEMS_AND_DELETED_DISJOINT", "deleted_items"),
            Map.entry("PUBLISH_REQUIREMENTS", "publish_requirements"),
            Map.entry("PASSWORDS_MATCH", "confirmPassword"),
            Map.entry("REJECT_NOTE_REQUIRED_WHEN_OTHER", "note"));

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ApiResponse(e.getErrorCode(), e.getData()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException e) {

        Stream<ValidationErrorRaw> stream = e.getConstraintViolations().stream().map(v -> {
            String propPath = v.getPropertyPath().toString();
            String rawField = propPath.contains(".")
                    ? propPath.substring(propPath.lastIndexOf('.') + 1)
                    : propPath;

            String ann = v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            String code = "AssertTrue".equals(ann) ? v.getMessageTemplate() : ann;
            return new ValidationErrorRaw(rawField, ann, code);
        });
        return ResponseEntity.badRequest().body(new ApiResponse("VALIDATION_ERROR", fold(stream)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Stream<ValidationErrorRaw> stream = e.getBindingResult().getFieldErrors().stream().map(fe -> {
            String ann = fe.getCode();
            String code = "AssertTrue".equals(ann) ? fe.getDefaultMessage() : ann;
            return new ValidationErrorRaw(fe.getField(), ann, code);
        });
        return ResponseEntity.badRequest().body(new ApiResponse("VALIDATION_ERROR", fold(stream)));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        // TODO:
        // errは現状FieldError型の前提
        Stream<ValidationErrorRaw> stream = e.getAllErrors().stream().map(err -> {
            FieldError fe = (FieldError) err;
            String ann = fe.getCode();
            String code = "AssertTrue".equals(ann) ? err.getDefaultMessage() : ann;

            return new ValidationErrorRaw(fe.getField(), ann, code);
        });
        return ResponseEntity.badRequest().body(new ApiResponse("VALIDATION_ERROR", fold(stream)));
    }

    private static String resolveField(String rawField, String ann, String code) {
        if ("AssertTrue".equals(ann)) {
            return RULE_KEY_TO_FIELD.get(code);
        }
        return rawField;
    }

    private static List<FieldErrorInfo> fold(Stream<ValidationErrorRaw> raws) {
        Map<String, Slot> best = new LinkedHashMap<>();
        raws.forEach(r -> {
            String field = resolveField(r.field, r.ann, r.code);
            int pr = PRIORITY.get(r.ann);
            best.merge(field, new Slot(r.code, pr),
                    (oldS, newS) -> newS.priority < oldS.priority ? newS : oldS);
        });
        return best.entrySet().stream()
                .map(b -> new FieldErrorInfo(b.getKey(), b.getValue().code))
                .toList();
    }

    @AllArgsConstructor
    private static final class ValidationErrorRaw {
        private final String field;
        private final String ann;
        private final String code;
    }

    @AllArgsConstructor
    private static final class Slot {
        private final String code;
        private final int priority;
    }

}
