package com.tenx.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_SCALE;
import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_PRECISION;

@Data
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private long version;

    @Column(precision = DECIMAL_DIGITS_PRECISION, scale = DECIMAL_DIGITS_SCALE)
    private BigDecimal balance;

    private Currency currency;

    private final long createAtTimestamp = System.currentTimeMillis();

    public Account() {

    }

    public Account(BigDecimal balance, Currency currency) {
        this.balance = balance;
        this.currency = currency;
    }
}
