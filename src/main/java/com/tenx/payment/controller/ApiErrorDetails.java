package com.tenx.payment.controller;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ApiErrorDetails {

    private final int httpCode;
    private final List<String> messages;
    private final LocalDateTime time;

    public ApiErrorDetails(int httpCode, LocalDateTime time, String... messages) {
        this.httpCode = httpCode;
        this.time = time;
        this.messages = List.of(messages);
    }
}