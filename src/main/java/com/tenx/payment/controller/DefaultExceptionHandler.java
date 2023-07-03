package com.tenx.payment.controller;

import com.tenx.payment.exception.ApiException;
import com.tenx.payment.exception.UnsupportedCurrencyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ae) {
        return getApiErrorDetailsResponseEntityForException(ae.getHttpCode(), ae.getTime(), ae.getMessage());
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<Object> handleUnsupportedCurrencyException(UnsupportedCurrencyException uce) {
        return getApiErrorDetailsResponseEntityForException(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), uce.getMessage());
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + " field : "+ error.getDefaultMessage());
        }
        return getApiErrorDetailsResponseEntityForException(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), errors.toArray(new String[0]));
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return getApiErrorDetailsResponseEntityForException(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), ex.getMessage());
    }

    private ResponseEntity<Object> getApiErrorDetailsResponseEntityForException(int statusCode, LocalDateTime time, String...errorMessages) {
        ApiErrorDetails apiErrorDetails = new ApiErrorDetails(statusCode, time, errorMessages);
        return new ResponseEntity<>(apiErrorDetails, HttpStatusCode.valueOf(statusCode));
    }
}
