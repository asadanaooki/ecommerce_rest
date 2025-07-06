package com.example.advice;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ApiResponse(e.getErrorCode(), e.getData()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException e) {
        // TODO:
        // 将来的に各フィールドfail-fastのバリデーションチェックにする
        List<FieldErrorInfo> errors = e.getConstraintViolations().stream()
                .map(v -> {
                    String propPath = v.getPropertyPath().toString();
                    String fieldName = propPath.contains(".")
                            ? propPath.substring(propPath.lastIndexOf('.') + 1)
                            : propPath;
                    String annotation = v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();

                    return new FieldErrorInfo(fieldName, annotation);
                }).toList();

        return ResponseEntity.badRequest()
                .body(new ApiResponse("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldErrorInfo> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorInfo(error.getField(), error.getCode())).toList();

        return ResponseEntity.badRequest()
                .body(new ApiResponse("VALIDATION_ERROR", errors));
    }

}
