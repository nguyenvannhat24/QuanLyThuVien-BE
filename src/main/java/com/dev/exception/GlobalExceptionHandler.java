package com.dev.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
            MethodArgumentNotValidException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Dữ liệu gửi lên không hợp lệ"
                ));
    }
}