package com.example.memer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


// @ConstrollerAdvice + @ResponesBody 
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExcepion(HttpServletRequest request, Exception ex) {

        log.error("----------------------------------------------------------------------------------------");
        log.error("uri : {}, method : {}", request.getRequestURI(), request.getMethod());
        log.error("error : {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code("500")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.ok().body(response);

    }

    

    // 유효성 검증
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleExcepion(HttpServletRequest request, MethodArgumentNotValidException ex) {

        log.error("url : {}, method : {}", request.getRequestURL(), request.getMethod());

        log.error("error : {}", ex.getMessage());

        StringBuilder builder = new StringBuilder();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

            builder.append("[")
                    .append(fieldError.getField())
                    .append("] : ")
                    .append(fieldError.getDefaultMessage())
                    .append(", Provided value : [")
                    .append(fieldError.getRejectedValue())
                    .append("]");
        }

        ErrorResponse response = ErrorResponse.builder()
                .code("400")
                .message(builder.toString())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}