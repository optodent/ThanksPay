package com.tenx.payment.dto.account;

import com.tenx.payment.validator.AllowedCurrencies;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_PRECISION;
import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_SCALE;

@Data
@Builder
public class AccountRequestDto {

    @NotNull
    @PositiveOrZero
    @Digits(integer = DECIMAL_DIGITS_PRECISION, fraction = DECIMAL_DIGITS_SCALE)
    private BigDecimal balance;

    @NotNull
    @AllowedCurrencies
    private Currency currency;
}
