package com.tenx.payment.dto.transaction;

import com.tenx.payment.validator.AllowedCurrencies;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Currency;

import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_PRECISION;
import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_SCALE;

@Data
public class TransactionDto {

    @NonNull
    @Positive
    @Digits(integer = DECIMAL_DIGITS_PRECISION, fraction = DECIMAL_DIGITS_SCALE)
    private BigDecimal amount;

    @NonNull
    private Long sourceAccountId;

    @NonNull
    private Long targetAccountId;

    @NonNull
    @AllowedCurrencies
    private Currency currency;
}
