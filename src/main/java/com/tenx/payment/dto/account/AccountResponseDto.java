package com.tenx.payment.dto.account;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

@Data
public class AccountResponseDto {

    private final long id;

    private final BigDecimal balance;

    private final Currency currency;

    private final long createdAtTimestamp;
}
