package com.tenx.payment.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class AccountNotFoundException extends ApiException {

    public AccountNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
    }

    public AccountNotFoundException(String message, int httpCode) {
        super(message, httpCode, LocalDateTime.now());
    }
}
