package com.tenx.payment.dto.transaction;

import java.math.BigDecimal;
import java.util.Currency;

public class TransactionResponseDto extends TransactionRequestDto {

    private final long id;

    public TransactionResponseDto(BigDecimal amount, Long sourceAccountId, Long targetAccountId, Currency currency, long id) {
        super(amount, sourceAccountId, targetAccountId, currency);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
