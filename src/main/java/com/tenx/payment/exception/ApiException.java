package com.tenx.payment.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiException extends RuntimeException {

    private final int httpCode;

    private final LocalDateTime time;

    public ApiException(String message, int httpCode, LocalDateTime time) {
        super(message);
        this.httpCode = httpCode;
        this.time = time;
    }
}
