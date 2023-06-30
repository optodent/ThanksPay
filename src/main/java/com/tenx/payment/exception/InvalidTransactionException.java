package com.tenx.payment.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class InvalidTransactionException extends ApiException {


    public InvalidTransactionException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }

    public InvalidTransactionException(String message, int httpCode) {
        super(message, httpCode, LocalDateTime.now());
    }
}
