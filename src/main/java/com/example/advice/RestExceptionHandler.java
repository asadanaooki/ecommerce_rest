package com.example.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.exception.BusinessException;
import com.example.response.ApiResponse;

import lombok.AllArgsConstructor;

@RestControllerAdvice
@AllArgsConstructor
public class RestExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ApiResponse(e.getErrorCode(), e.getData())) ;
    }
}
